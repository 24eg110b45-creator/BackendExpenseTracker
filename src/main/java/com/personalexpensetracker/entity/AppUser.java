package com.personalexpensetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    protected AppUser() {}

    public AppUser(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getCurrency() { return currency; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setCurrency(String currency) { this.currency = currency; }
}
