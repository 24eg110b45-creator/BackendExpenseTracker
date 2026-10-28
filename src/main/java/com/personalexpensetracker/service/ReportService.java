package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.ReportResponse;
import com.personalexpensetracker.entity.ExpenseTransaction;
import com.personalexpensetracker.entity.TransactionType;
import com.personalexpensetracker.exception.BadRequestException;
import com.personalexpensetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final TransactionRepository transactionRepository;
    private final UserContext userContext;

    public ReportService(TransactionRepository transactionRepository, UserContext userContext) {
        this.transactionRepository = transactionRepository;
        this.userContext = userContext;
    }

    @Transactional(readOnly = true)
    public ReportResponse report(LocalDate start, LocalDate end, String groupBy) {
        if (start.isAfter(end)) throw new BadRequestException("Start date must be on or before end date.");
        if (start.plusYears(5).isBefore(end)) throw new BadRequestException("Report date range cannot exceed five years.");
        if (!Set.of("daily", "weekly", "monthly").contains(groupBy)) {
            throw new BadRequestException("groupBy must be daily, weekly, or monthly.");
        }
        List<ExpenseTransaction> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetween(userContext.currentUser().getId(), start, end);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        Map<LocalDate, BigDecimal[]> periods = new TreeMap<>();
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        Map<String, String> categoryColors = new HashMap<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        for (ExpenseTransaction transaction : transactions) {
            LocalDate bucketDate = bucketDate(transaction.getTransactionDate(), groupBy);
            BigDecimal[] totals = periods.computeIfAbsent(bucketDate,
                    ignored -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (transaction.getType() == TransactionType.INCOME) {
                income = income.add(transaction.getAmount());
                totals[0] = totals[0].add(transaction.getAmount());
            } else {
                expenses = expenses.add(transaction.getAmount());
                totals[1] = totals[1].add(transaction.getAmount());
                String category = transaction.getCategory().getName();
                categoryTotals.merge(category, transaction.getAmount(), BigDecimal::add);
                categoryColors.put(category, transaction.getCategory().getColor());
            }
        }
        List<ReportResponse.PeriodTotal> periodTotals = periods.entrySet().stream()
                .map(entry -> new ReportResponse.PeriodTotal(entry.getKey().format(labelFormat),
                        entry.getValue()[0], entry.getValue()[1])).toList();
        List<ReportResponse.CategoryTotal> categories = categoryTotals.entrySet().stream()
                .map(entry -> new ReportResponse.CategoryTotal(entry.getKey(), categoryColors.get(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing(ReportResponse.CategoryTotal::amount).reversed()).toList();
        return new ReportResponse(start, end, income, expenses, income.subtract(expenses), periodTotals, categories);
    }

    private LocalDate bucketDate(LocalDate date, String groupBy) {
        return switch (groupBy) {
            case "weekly" -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "monthly" -> date.withDayOfMonth(1);
            default -> date;
        };
    }
}
