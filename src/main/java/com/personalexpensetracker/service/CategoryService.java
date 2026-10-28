package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.CategoryRequest;
import com.personalexpensetracker.dto.CategoryResponse;
import com.personalexpensetracker.entity.AppUser;
import com.personalexpensetracker.entity.Category;
import com.personalexpensetracker.exception.ConflictException;
import com.personalexpensetracker.exception.NotFoundException;
import com.personalexpensetracker.repository.BudgetRepository;
import com.personalexpensetracker.repository.CategoryRepository;
import com.personalexpensetracker.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserContext userContext;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository,
            BudgetRepository budgetRepository, UserContext userContext) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.userContext = userContext;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAllByUserIdOrderByName(userContext.currentUser().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        AppUser user = userContext.currentUser();
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), user.getId())) {
            throw new ConflictException("A category with this name already exists.");
        }
        return toResponse(categoryRepository.save(new Category(request.name().trim(), request.type(),
                request.color(), user)));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOwned(id);
        if (!category.getName().equalsIgnoreCase(request.name().trim())
                && categoryRepository.existsByNameIgnoreCaseAndUserId(request.name().trim(), category.getUser().getId())) {
            throw new ConflictException("A category with this name already exists.");
        }
        if (category.getType() != request.type() && transactionRepository.existsByCategoryId(id)) {
            throw new ConflictException("A category type cannot change while transactions use it.");
        }
        category.setName(request.name().trim());
        category.setType(request.type());
        category.setColor(request.color());
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOwned(id);
        if (transactionRepository.existsByCategoryId(id) || budgetRepository.existsByCategoryId(id)) {
            throw new ConflictException("Delete or reassign this category's transactions and budgets first.");
        }
        categoryRepository.delete(category);
    }

    private Category findOwned(Long id) {
        return categoryRepository.findByIdAndUserId(id, userContext.currentUser().getId())
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.getColor());
    }
}
