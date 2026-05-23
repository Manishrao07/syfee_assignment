package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.MonthlyReportResponse;
import com.syfe.finance_manager.dto.YearlyReportResponse;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.BadRequestException;
import com.syfe.finance_manager.repository.TransactionRepository;
import com.syfe.finance_manager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ReportController(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        
        User user = userService.getAuthenticatedUser();

        // Validate Month
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                user.getId(), startDate, endDate
        );

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amt = t.getAmount();

            if (t.getType() == CategoryType.INCOME) {
                totalIncome.put(catName, totalIncome.getOrDefault(catName, BigDecimal.ZERO).add(amt));
                sumIncome = sumIncome.add(amt);
            } else {
                totalExpenses.put(catName, totalExpenses.getOrDefault(catName, BigDecimal.ZERO).add(amt));
                sumExpense = sumExpense.add(amt);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return ResponseEntity.ok(new MonthlyReportResponse(
                month, year, totalIncome, totalExpenses, netSavings
        ));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        User user = userService.getAuthenticatedUser();

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                user.getId(), startDate, endDate
        );

        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amt = t.getAmount();

            if (t.getType() == CategoryType.INCOME) {
                totalIncome.put(catName, totalIncome.getOrDefault(catName, BigDecimal.ZERO).add(amt));
                sumIncome = sumIncome.add(amt);
            } else {
                totalExpenses.put(catName, totalExpenses.getOrDefault(catName, BigDecimal.ZERO).add(amt));
                sumExpense = sumExpense.add(amt);
            }
        }

        BigDecimal netSavings = sumIncome.subtract(sumExpense);

        return ResponseEntity.ok(new YearlyReportResponse(
                year, totalIncome, totalExpenses, netSavings
        ));
    }
}
