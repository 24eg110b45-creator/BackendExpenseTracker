package com.personalexpensetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets", indexes = @Index(name = "idx_budget_user_period", columnList = "user_id, budget_year, budget_month"))
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    protected Budget() {}

    public Budget(BigDecimal amount, int year, int month, Category category, AppUser user) {
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.category = category;
        this.user = user;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public Category getCategory() { return category; }
    public AppUser getUser() { return user; }
    public void update(BigDecimal amount, int year, int month, Category category) {
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.category = category;
    }
}
