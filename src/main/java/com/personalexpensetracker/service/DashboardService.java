package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.DashboardResponse;
import com.personalexpensetracker.dto.TransactionResponse;
import com.personalexpensetracker.entity.ExpenseTransaction;
import com.personalexpensetracker.entity.TransactionType;
import com.personalexpensetracker.repository.BudgetRepository;
import com.personalexpensetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserContext userContext;
    private final TransactionService transactionService;

    public DashboardService(TransactionRepository transactionRepository, BudgetRepository budgetRepository,
            UserContext userContext, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.userContext = userContext;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        Long userId = userContext.currentUser().getId();
        YearMonth current = YearMonth.now();
        LocalDate start = current.atDay(1);
        LocalDate end = current.atEndOfMonth();
        BigDecimal income = transactionRepository.sumForPeriod(userId, TransactionType.INCOME, start, end);
        BigDecimal expenses = transactionRepository.sumForPeriod(userId, TransactionType.EXPENSE, start, end);
        BigDecimal monthlyBudget = budgetRepository.findAllByUserIdAndYearAndMonthOrderById(
                        userId, current.getYear(), current.getMonthValue()).stream()
                .filter(budget -> budget.getCategory() == null)
                .map(budget -> budget.getAmount()).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal remaining = monthlyBudget.subtract(expenses);
        int percent = monthlyBudget.signum() == 0 ? 0
                : expenses.multiply(BigDecimal.valueOf(100)).divide(monthlyBudget, 0, RoundingMode.HALF_UP).intValue();
        String budgetStatus = monthlyBudget.signum() == 0 ? "NOT_SET"
                : percent >= 100 ? "EXCEEDED" : percent >= 80 ? "WARNING" : "ON_TRACK";

        List<ExpenseTransaction> monthTransactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetween(userId, start, end);
        Map<Long, BigDecimal> categoryTotals = new LinkedHashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, String> categoryColors = new HashMap<>();
        for (ExpenseTransaction transaction : monthTransactions) {
            if (transaction.getType() == TransactionType.EXPENSE) {
                Long categoryId = transaction.getCategory().getId();
                categoryTotals.merge(categoryId, transaction.getAmount(), BigDecimal::add);
                categoryNames.put(categoryId, transaction.getCategory().getName());
                categoryColors.put(categoryId, transaction.getCategory().getColor());
            }
        }
        List<DashboardResponse.CategoryTotal> categories = categoryTotals.entrySet().stream()
                .map(entry -> new DashboardResponse.CategoryTotal(categoryNames.get(entry.getKey()),
                        categoryColors.get(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing(DashboardResponse.CategoryTotal::amount).reversed()).toList();

        List<DashboardResponse.TrendPoint> trend = new ArrayList<>();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            trend.add(new DashboardResponse.TrendPoint(
                    month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    transactionRepository.sumForPeriod(userId, TransactionType.INCOME, month.atDay(1), month.atEndOfMonth()),
                    transactionRepository.sumForPeriod(userId, TransactionType.EXPENSE, month.atDay(1), month.atEndOfMonth())));
        }
        List<TransactionResponse> recent = transactionRepository.findTop8ByUserIdOrderByTransactionDateDescIdDesc(userId)
                .stream().map(transactionService::toResponse).toList();
        return new DashboardResponse(new DashboardResponse.Summary(income, expenses, income.subtract(expenses),
                monthlyBudget, remaining, percent, budgetStatus), trend, categories, recent);
    }
}
