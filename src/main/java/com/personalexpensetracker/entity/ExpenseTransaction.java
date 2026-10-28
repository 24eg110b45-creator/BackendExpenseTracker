package com.personalexpensetracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_transaction_category", columnList = "category_id")
})
public class ExpenseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    protected ExpenseTransaction() {}

    public ExpenseTransaction(String title, BigDecimal amount, TransactionType type, Category category,
                              LocalDate transactionDate, String notes, AppUser user) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.transactionDate = transactionDate;
        this.notes = notes;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public Category getCategory() { return category; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getNotes() { return notes; }
    public AppUser getUser() { return user; }
    public void update(String title, BigDecimal amount, TransactionType type, Category category,
                       LocalDate transactionDate, String notes) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }
}
