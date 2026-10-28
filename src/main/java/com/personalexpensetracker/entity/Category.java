package com.personalexpensetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories", indexes = @Index(name = "idx_category_user", columnList = "user_id"))
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @Column(length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    protected Category() {}

    public Category(String name, CategoryType type, String color, AppUser user) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public String getColor() { return color; }
    public AppUser getUser() { return user; }
    public void setName(String name) { this.name = name; }
    public void setType(CategoryType type) { this.type = type; }
    public void setColor(String color) { this.color = color; }
}
