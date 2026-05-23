package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.CategoriesListResponse;
import com.syfe.finance_manager.dto.CategoryRequest;
import com.syfe.finance_manager.dto.CategoryResponse;
import com.syfe.finance_manager.dto.MessageResponse;
import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BadRequestException;
import com.syfe.finance_manager.exception.ConflictException;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.repository.CategoryRepository;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public CategoryController(CategoryRepository categoryRepository, 
                              TransactionRepository transactionRepository, 
                              UserService userService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CategoriesListResponse> getCategories() {
        User user = userService.getAuthenticatedUser();
        List<Category> categories = categoryRepository.findByDefaultOrUserId(user.getId());
        
        List<CategoryResponse> dtos = categories.stream()
                .map(c -> new CategoryResponse(c.getName(), c.getType(), c.isCustom()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new CategoriesListResponse(dtos));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        User user = userService.getAuthenticatedUser();
        
        // Check if duplicate category name exists for this user (custom) or globally (default)
        if (categoryRepository.existsByNameAndUserIsNull(request.getName()) || 
            categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new ConflictException("Category already exists");
        }

        Category category = new Category(
                request.getName(),
                request.getType(),
                true,
                user
        );

        Category savedCategory = categoryRepository.save(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CategoryResponse(savedCategory.getName(), savedCategory.getType(), savedCategory.isCustom()));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        User user = userService.getAuthenticatedUser();
        
        // Find category either as a default category or as a custom category owned by the current user
        Category category = categoryRepository.findByNameAndUserOrUserId(name, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // If it's a default category, prevent deletion
        if (!category.isCustom()) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

        // If it belongs to another user (should not happen due to the query, but to be safe)
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not own this category");
        }

        // Check if any transaction is referencing this category
        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new BadRequestException("Categories currently referenced by transactions cannot be deleted");
        }

        categoryRepository.delete(category);

        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
