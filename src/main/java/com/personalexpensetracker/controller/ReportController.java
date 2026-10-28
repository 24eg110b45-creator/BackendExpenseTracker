package com.personalexpensetracker.controller;

import com.personalexpensetracker.dto.ReportResponse;
import com.personalexpensetracker.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ReportResponse monthly(@RequestParam(required = false) @Min(2000) @Max(2100) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {
        YearMonth selected = YearMonth.of(year == null ? LocalDate.now().getYear() : year,
                month == null ? LocalDate.now().getMonthValue() : month);
        return reportService.report(selected.atDay(1), selected.atEndOfMonth(), "daily");
    }

    @GetMapping
    public ReportResponse report(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "monthly") String groupBy) {
        LocalDate today = LocalDate.now();
        return reportService.report(startDate == null ? today.withDayOfMonth(1) : startDate,
                endDate == null ? today : endDate, groupBy);
    }
}
