package com.syfe.finance_manager.repository;

import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Check if category is used in any transaction
    boolean existsByCategoryId(Long categoryId);

    // Filter transactions with optional fields
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:categoryName IS NULL OR t.category.name = :categoryName) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findFilteredTransactions(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("categoryId") Long categoryId,
        @Param("categoryName") String categoryName,
        @Param("type") CategoryType type
    );

    // Calculate progress for a goal: sum(income) - sum(expense) since startDate
    @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0.00) " +
           "FROM Transaction t WHERE t.user.id = :userId AND t.date >= :startDate")
    BigDecimal calculateNetSavingsSince(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate
    );

    // Find transactions for monthly and yearly reports
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
