package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.SettingsRequest;
import com.personalexpensetracker.dto.SettingsResponse;
import com.personalexpensetracker.entity.AppUser;
import com.personalexpensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {
    private final UserContext userContext;
    private final UserRepository userRepository;

    public SettingsService(UserContext userContext, UserRepository userRepository) {
        this.userContext = userContext;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return response(userContext.currentUser());
    }

    @Transactional
    public SettingsResponse update(SettingsRequest request) {
        AppUser user = userContext.currentUser();
        user.setDisplayName(request.displayName().trim());
        user.setCurrency(request.currency());
        userRepository.save(user);
        return response(user);
    }

    private SettingsResponse response(AppUser user) {
        return new SettingsResponse(user.getDisplayName(), user.getEmail(), user.getCurrency());
    }
}
