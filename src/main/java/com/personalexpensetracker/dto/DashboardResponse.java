package com.personalexpensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(Summary summary, List<TrendPoint> trend,
        List<CategoryTotal> categories, List<TransactionResponse> recent) {
    public record Summary(BigDecimal income, BigDecimal expenses, BigDecimal balance,
            BigDecimal monthlyBudget, BigDecimal budgetRemaining, int budgetPercent, String budgetStatus) {}
    public record TrendPoint(String month, BigDecimal income, BigDecimal expenses) {}
    public record CategoryTotal(String category, String color, BigDecimal amount) {}
}
