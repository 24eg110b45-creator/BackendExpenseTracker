package com.personalexpensetracker.dto;

import com.personalexpensetracker.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(Long id, String title, BigDecimal amount, TransactionType type,
        Long categoryId, String category, String categoryColor, LocalDate transactionDate, String notes) {
}
