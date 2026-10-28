package com.personalexpensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SettingsRequest(@NotBlank @jakarta.validation.constraints.Size(max = 100) String displayName,
        @NotBlank @Pattern(regexp = "USD|EUR|GBP|INR|CAD|AUD") String currency) {
}
