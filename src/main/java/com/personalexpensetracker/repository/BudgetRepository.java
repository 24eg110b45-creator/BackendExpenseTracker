package com.personalexpensetracker.repository;

import com.personalexpensetracker.entity.Budget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUserIdAndYearAndMonthOrderById(Long userId, int year, int month);
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    boolean existsByCategoryId(Long categoryId);
}
