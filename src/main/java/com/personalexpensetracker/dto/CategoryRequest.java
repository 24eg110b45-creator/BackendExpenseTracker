package com.personalexpensetracker.dto;

import com.personalexpensetracker.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(@NotBlank @Size(max = 60) String name,
        @NotNull CategoryType type, @Size(max = 20) String color) {
}
