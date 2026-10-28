package com.personalexpensetracker.dto;

import java.math.BigDecimal;

public record BudgetResponse(Long id, BigDecimal amount, int year, int month,
        Long categoryId, String category, BigDecimal spent, BigDecimal remaining) {
}
