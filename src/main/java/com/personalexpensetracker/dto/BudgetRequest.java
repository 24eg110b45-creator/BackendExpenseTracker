package com.personalexpensetracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetRequest(@NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
        @Min(2000) @Max(2100) int year, @Min(1) @Max(12) int month,
        @Positive Long categoryId) {
}
