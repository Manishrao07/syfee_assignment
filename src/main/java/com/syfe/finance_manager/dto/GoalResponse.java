package com.syfe.finance_manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class GoalResponse {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    private BigDecimal currentProgress;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;

    public GoalResponse() {}

    public GoalResponse(Long id, String goalName, BigDecimal targetAmount, LocalDate targetDate, 
                        LocalDate startDate, BigDecimal currentProgress) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = formatBigDecimal(targetAmount);
        this.targetDate = targetDate;
        this.startDate = startDate;
        this.currentProgress = formatBigDecimal(currentProgress);
        
        // Calculate remaining amount
        if (targetAmount != null && currentProgress != null) {
            BigDecimal remaining = targetAmount.subtract(currentProgress);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                this.remainingAmount = formatBigDecimal(BigDecimal.ZERO);
            } else {
                this.remainingAmount = formatBigDecimal(remaining);
            }
            
            // Calculate progress percentage with the dynamic scale logic to match E2E tests
            if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = currentProgress.multiply(new BigDecimal(100))
                                                    .divide(targetAmount, 4, RoundingMode.HALF_UP);
                BigDecimal pct2 = pct.setScale(2, RoundingMode.HALF_UP);
                BigDecimal pct1 = pct.setScale(1, RoundingMode.HALF_UP);
                if (pct2.compareTo(pct1) == 0) {
                    this.progressPercentage = pct1;
                } else {
                    this.progressPercentage = pct2;
                }
            } else {
                this.progressPercentage = BigDecimal.ZERO.setScale(1);
            }
        }
    }

    private BigDecimal formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(0); // Serializes as 0
        }
        return value.setScale(2, RoundingMode.HALF_UP); // Serializes as XXXX.00
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = formatBigDecimal(targetAmount);
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(BigDecimal currentProgress) {
        this.currentProgress = formatBigDecimal(currentProgress);
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = formatBigDecimal(remainingAmount);
    }
}
