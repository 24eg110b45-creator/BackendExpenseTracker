package com.personalexpensetracker.dto;

import com.personalexpensetracker.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
        @NotNull TransactionType type,
        @NotNull @Positive Long categoryId,
        @NotNull @PastOrPresent LocalDate transactionDate,
        @Size(max = 1000) String notes) {
}
