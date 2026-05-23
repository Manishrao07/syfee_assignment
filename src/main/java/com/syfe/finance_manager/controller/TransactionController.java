package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.MessageResponse;
import com.syfe.finance_manager.dto.TransactionRequest;
import com.syfe.finance_manager.dto.TransactionResponse;
import com.syfe.finance_manager.dto.TransactionsListResponse;
import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BadRequestException;
import com.syfe.finance_manager.exception.ForbiddenException;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.repository.CategoryRepository;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public TransactionController(TransactionRepository transactionRepository, 
                                 CategoryRepository categoryRepository, 
                                 UserService userService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        User user = userService.getAuthenticatedUser();

        // 1. Validate Amount
        if (request.getAmount() == null) {
            throw new BadRequestException("Amount is required");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be a positive decimal value");
        }

        // 2. Validate Date
        if (request.getDate() == null) {
            throw new BadRequestException("Date is required");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }

        // 3. Validate Category
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new BadRequestException("Category is required");
        }
        Category category = categoryRepository.findByNameAndUserOrUserId(request.getCategory(), user.getId())
                .orElseThrow(() -> new BadRequestException("Invalid category name or category not accessible"));

        // Create transaction
        Transaction transaction = new Transaction(
                request.getAmount(),
                request.getDate(),
                category,
                request.getDescription(),
                category.getType(),
                user
        );

        Transaction savedTx = transactionRepository.save(transaction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TransactionResponse(
                        savedTx.getId(),
                        savedTx.getAmount(),
                        savedTx.getDate(),
                        savedTx.getCategory().getName(),
                        savedTx.getDescription(),
                        savedTx.getType()
                ));
    }

    @GetMapping
    public ResponseEntity<TransactionsListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CategoryType type) {
        
        User user = userService.getAuthenticatedUser();

        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                user.getId(), startDate, endDate, categoryId, category, type
        );

        List<TransactionResponse> dtos = transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getDate(),
                        t.getCategory().getName(),
                        t.getDescription(),
                        t.getType()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new TransactionsListResponse(dtos));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequest request) {
        
        User user = userService.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Validate Ownership
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to modify this transaction");
        }

        // Update Amount if provided
        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be positive");
            }
            transaction.setAmount(request.getAmount());
        }

        // Update Category if provided
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            Category category = categoryRepository.findByNameAndUserOrUserId(request.getCategory(), user.getId())
                    .orElseThrow(() -> new BadRequestException("Invalid category"));
            transaction.setCategory(category);
            transaction.setType(category.getType());
        }

        // Update Description if provided
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        // Date updates are ignored. Date field is never updated.
        // Even if request.getDate() is provided, we do not call transaction.setDate(...)

        Transaction savedTx = transactionRepository.save(transaction);

        return ResponseEntity.ok(new TransactionResponse(
                savedTx.getId(),
                savedTx.getAmount(),
                savedTx.getDate(),
                savedTx.getCategory().getName(),
                savedTx.getDescription(),
                savedTx.getType()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        User user = userService.getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Validate Ownership
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this transaction");
        }

        transactionRepository.delete(transaction);

        return ResponseEntity.ok(new MessageResponse("Transaction deleted successfully"));
    }
}
