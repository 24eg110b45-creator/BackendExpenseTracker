package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.BudgetRequest;
import com.personalexpensetracker.dto.BudgetResponse;
import com.personalexpensetracker.entity.*;
import com.personalexpensetracker.exception.ConflictException;
import com.personalexpensetracker.exception.NotFoundException;
import com.personalexpensetracker.repository.BudgetRepository;
import com.personalexpensetracker.repository.CategoryRepository;
import com.personalexpensetracker.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserContext userContext;

    public BudgetService(BudgetRepository budgetRepository, CategoryRepository categoryRepository,
            TransactionRepository transactionRepository, UserContext userContext) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userContext = userContext;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int selectedYear = year == null ? now.getYear() : year;
        int selectedMonth = month == null ? now.getMonthValue() : month;
        return budgetRepository.findAllByUserIdAndYearAndMonthOrderById(userContext.currentUser().getId(),
                selectedYear, selectedMonth).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse get(Long id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userContext.currentUser().getId())
                .orElseThrow(() -> new NotFoundException("Budget not found."));
        return toResponse(budget);
    }

    @Transactional
    public BudgetResponse save(BudgetRequest request) {
        AppUser user = userContext.currentUser();
        Category category = request.categoryId() == null ? null : categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Category not found."));
        if (category != null && category.getType() == CategoryType.INCOME) {
            throw new ConflictException("Budgets can only be assigned to expense categories.");
        }
        List<Budget> matching = budgetRepository.findAllByUserIdAndYearAndMonthOrderById(
                user.getId(), request.year(), request.month());
        Budget budget = matching.stream()
                .filter(item -> sameCategory(item.getCategory(), category))
                .findFirst().orElse(null);
        if (budget == null) {
            budget = new Budget(request.amount(), request.year(), request.month(), category, user);
        } else {
            budget.update(request.amount(), request.year(), request.month(), category);
        }
        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userContext.currentUser().getId())
                .orElseThrow(() -> new NotFoundException("Budget not found."));
        AppUser user = userContext.currentUser();
        Category category = request.categoryId() == null ? null : categoryRepository
                .findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new NotFoundException("Category not found."));
        if (category != null && category.getType() == CategoryType.INCOME) {
            throw new ConflictException("Budgets can only be assigned to expense categories.");
        }
        budget.update(request.amount(), request.year(), request.month(), category);
        return toResponse(budget);
    }

    @Transactional
    public void delete(Long id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userContext.currentUser().getId())
                .orElseThrow(() -> new NotFoundException("Budget not found."));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        LocalDate start = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = transactionRepository.expenseTotal(budget.getUser().getId(), TransactionType.EXPENSE, start, end,
                budget.getCategory() == null ? null : budget.getCategory().getId());
        return new BudgetResponse(budget.getId(), budget.getAmount(), budget.getYear(), budget.getMonth(),
                budget.getCategory() == null ? null : budget.getCategory().getId(),
                budget.getCategory() == null ? null : budget.getCategory().getName(),
                spent, budget.getAmount().subtract(spent));
    }

    private boolean sameCategory(Category first, Category second) {
        return first == null ? second == null : second != null && first.getId().equals(second.getId());
    }
}
