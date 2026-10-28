package com.personalexpensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.personalexpensetracker.dto.ReportResponse;
import com.personalexpensetracker.entity.AppUser;
import com.personalexpensetracker.entity.Category;
import com.personalexpensetracker.entity.CategoryType;
import com.personalexpensetracker.entity.ExpenseTransaction;
import com.personalexpensetracker.entity.TransactionType;
import com.personalexpensetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportServiceTest {
    @Test
    void weeklyReportGroupsTransactionsAndTotalsExpensesOnlyByCategory() {
        TransactionRepository transactions = mock(TransactionRepository.class);
        UserContext userContext = mock(UserContext.class);
        AppUser user = new AppUser("you@example.com", "Your name");
        Category food = new Category("Food", CategoryType.EXPENSE, "#f59e0b", user);
        Category salary = new Category("Salary", CategoryType.INCOME, "#22c55e", user);
        LocalDate start = LocalDate.of(2026, 10, 26);
        LocalDate end = LocalDate.of(2026, 11, 1);
        when(userContext.currentUser()).thenReturn(user);
        when(transactions.findAllByUserIdAndTransactionDateBetween(user.getId(), start, end))
                .thenReturn(List.of(
                        new ExpenseTransaction("Groceries", new BigDecimal("25.50"), TransactionType.EXPENSE,
                                food, start, null, user),
                        new ExpenseTransaction("Paycheck", new BigDecimal("1200.00"), TransactionType.INCOME,
                                salary, start.plusDays(1), null, user),
                        new ExpenseTransaction("Lunch", new BigDecimal("14.50"), TransactionType.EXPENSE,
                                food, start.plusDays(2), null, user)));

        ReportResponse report = new ReportService(transactions, userContext).report(start, end, "weekly");

        assertEquals("1200.00", report.income().toPlainString());
        assertEquals("40.00", report.expenses().toPlainString());
        assertEquals("1160.00", report.balance().toPlainString());
        assertEquals(1, report.periods().size());
        assertEquals("Oct 26, 2026", report.periods().get(0).period());
        assertEquals(1, report.categories().size());
        assertEquals("Food", report.categories().get(0).category());
        assertEquals("40.00", report.categories().get(0).amount().toPlainString());
    }
}
