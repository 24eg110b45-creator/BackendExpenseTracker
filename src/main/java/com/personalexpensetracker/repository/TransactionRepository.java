package com.personalexpensetracker.repository;

import com.personalexpensetracker.entity.ExpenseTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<ExpenseTransaction, Long>,
        JpaSpecificationExecutor<ExpenseTransaction> {
    Optional<ExpenseTransaction> findByIdAndUserId(Long id, Long userId);
    List<ExpenseTransaction> findAllByUserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);
    List<ExpenseTransaction> findTop8ByUserIdOrderByTransactionDateDescIdDesc(Long userId);
    @Query("select coalesce(sum(t.amount), 0) from ExpenseTransaction t where t.user.id = :userId "
            + "and t.type = :type and t.transactionDate between :start and :end")
    BigDecimal sumForPeriod(@Param("userId") Long userId,
            @Param("type") com.personalexpensetracker.entity.TransactionType type,
            @Param("start") LocalDate start, @Param("end") LocalDate end);
    @Query("select coalesce(sum(t.amount), 0) from ExpenseTransaction t where t.user.id = :userId "
            + "and t.type = :type "
            + "and t.transactionDate between :start and :end "
            + "and (:categoryId is null or t.category.id = :categoryId)")
    BigDecimal expenseTotal(@Param("userId") Long userId,
            @Param("type") com.personalexpensetracker.entity.TransactionType type,
            @Param("start") LocalDate start, @Param("end") LocalDate end, @Param("categoryId") Long categoryId);
    boolean existsByCategoryId(Long categoryId);
}
