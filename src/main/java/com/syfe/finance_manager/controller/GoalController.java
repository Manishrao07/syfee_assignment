package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.GoalRequest;
import com.syfe.finance_manager.dto.GoalResponse;
import com.syfe.finance_manager.dto.GoalsListResponse;
import com.syfe.finance_manager.dto.MessageResponse;
import com.syfe.finance_manager.entity.Goal;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BadRequestException;
import com.syfe.finance_manager.exception.ForbiddenException;
import com.syfe.finance_manager.exception.ResourceNotFoundException;
import com.syfe.finance_manager.repository.GoalRepository;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public GoalController(GoalRepository goalRepository, 
                          TransactionRepository transactionRepository, 
                          UserService userService) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@RequestBody GoalRequest request) {
        User user = userService.getAuthenticatedUser();

        // 1. Validate Goal Name
        if (request.getGoalName() == null || request.getGoalName().isBlank()) {
            throw new BadRequestException("Goal name is required");
        }

        // 2. Validate Target Amount
        if (request.getTargetAmount() == null) {
            throw new BadRequestException("Target amount is required");
        }
        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be positive");
        }

        // 3. Validate Target Date
        if (request.getTargetDate() == null) {
            throw new BadRequestException("Target date is required");
        }
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        // 4. Validate Start Date
        LocalDate startDate = request.getStartDate();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (startDate.isAfter(request.getTargetDate())) {
            throw new BadRequestException("Start date cannot be after target date");
        }

        Goal goal = new Goal(
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                startDate,
                user
        );

        Goal savedGoal = goalRepository.save(goal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(savedGoal));
    }

    @GetMapping
    public ResponseEntity<GoalsListResponse> getGoals() {
        User user = userService.getAuthenticatedUser();
        List<Goal> goals = goalRepository.findByUserId(user.getId());
        
        List<GoalResponse> dtos = goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GoalsListResponse(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to view this goal");
        }

        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @RequestBody GoalRequest request) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to modify this goal");
        }

        // Validate updates if provided
        if (request.getGoalName() != null && !request.getGoalName().isBlank()) {
            goal.setGoalName(request.getGoalName());
        }

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be positive");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }

        // Final sanity check of dates
        if (goal.getStartDate().isAfter(goal.getTargetDate())) {
            throw new BadRequestException("Start date cannot be after target date");
        }

        Goal savedGoal = goalRepository.save(goal);

        return ResponseEntity.ok(mapToResponse(savedGoal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this goal");
        }

        goalRepository.delete(goal);

        return ResponseEntity.ok(new MessageResponse("Goal deleted successfully"));
    }

    private GoalResponse mapToResponse(Goal goal) {
        BigDecimal progress = transactionRepository.calculateNetSavingsSince(
                goal.getUser().getId(), goal.getStartDate()
        );
        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                progress
        );
    }
}
