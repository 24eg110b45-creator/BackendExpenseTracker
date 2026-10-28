package com.personalexpensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportResponse(LocalDate startDate, LocalDate endDate, BigDecimal income,
        BigDecimal expenses, BigDecimal balance, List<PeriodTotal> periods,
        List<CategoryTotal> categories) {
    public record PeriodTotal(String period, BigDecimal income, BigDecimal expenses) {}
    public record CategoryTotal(String category, String color, BigDecimal amount) {}
}
