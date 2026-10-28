package com.personalexpensetracker.config;

import com.personalexpensetracker.entity.AppUser;
import com.personalexpensetracker.entity.Category;
import com.personalexpensetracker.entity.CategoryType;
import com.personalexpensetracker.repository.CategoryRepository;
import com.personalexpensetracker.repository.UserRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AppDataInitializer(UserRepository userRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        AppUser user = userRepository.findByEmailIgnoreCase("you@example.com")
                .orElseGet(() -> userRepository.save(new AppUser("you@example.com", "Your name")));
        List<Object[]> defaults = List.of(
                new Object[]{"Food", CategoryType.EXPENSE, "#f59e0b"},
                new Object[]{"Travel", CategoryType.EXPENSE, "#06b6d4"},
                new Object[]{"Shopping", CategoryType.EXPENSE, "#ec4899"},
                new Object[]{"Education", CategoryType.EXPENSE, "#8b5cf6"},
                new Object[]{"Bills", CategoryType.EXPENSE, "#64748b"},
                new Object[]{"Healthcare", CategoryType.EXPENSE, "#ef4444"},
                new Object[]{"Entertainment", CategoryType.EXPENSE, "#a855f7"},
                new Object[]{"Salary", CategoryType.INCOME, "#22c55e"},
                new Object[]{"Freelance", CategoryType.INCOME, "#14b8a6"},
                new Object[]{"Other", CategoryType.BOTH, "#94a3b8"});
        for (Object[] item : defaults) {
            String name = (String) item[0];
            if (!categoryRepository.existsByNameIgnoreCaseAndUserId(name, user.getId())) {
                categoryRepository.save(new Category(name, (CategoryType) item[1], (String) item[2], user));
            }
        }
    }
}
