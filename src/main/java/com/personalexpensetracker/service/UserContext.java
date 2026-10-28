package com.personalexpensetracker.service;

import com.personalexpensetracker.entity.AppUser;
import com.personalexpensetracker.exception.NotFoundException;
import com.personalexpensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserContext {
    public static final String DEFAULT_USER_EMAIL = "you@example.com";
    private final UserRepository userRepository;

    public UserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser currentUser() {
        return userRepository.findByEmailIgnoreCase(DEFAULT_USER_EMAIL)
                .orElseThrow(() -> new NotFoundException("Default user has not been initialized."));
    }
}
