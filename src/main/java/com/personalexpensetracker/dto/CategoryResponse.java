package com.personalexpensetracker.dto;

import com.personalexpensetracker.entity.CategoryType;

public record CategoryResponse(Long id, String name, CategoryType type, String color) {
}
