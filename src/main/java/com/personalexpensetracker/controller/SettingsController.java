package com.personalexpensetracker.controller;

import com.personalexpensetracker.dto.SettingsRequest;
import com.personalexpensetracker.dto.SettingsResponse;
import com.personalexpensetracker.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse get() { return settingsService.get(); }

    @PutMapping
    public SettingsResponse update(@Valid @RequestBody SettingsRequest request) {
        return settingsService.update(request);
    }
}
