package com.personalexpensetracker.service;

import com.personalexpensetracker.dto.TransactionRequest;
import com.personalexpensetracker.dto.TransactionResponse;
import com.personalexpensetracker.entity.*;
import com.personalexpensetracker.exception.ConflictException;
import com.personalexpensetracker.exception.BadRequestException;
import com.personalexpensetracker.exception.NotFoundException;
import com.personalexpensetracker.repository.CategoryRepository;
import com.personalexpensetracker.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserContext userContext;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
                              UserContext userContext) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userContext = userContext;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(String search, TransactionType type, Long categoryId,
            LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String direction) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be on or before end date.");
        }
        String sortField = switch (sortBy) {
            case "amount" -> "amount";
            case "title" -> "title";
            default -> "transactionDate";
        };
        Sort sort = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortField)
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        Long userId = userContext.currentUser().getId();
        Specification<ExpenseTransaction> spec = (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("notes")), pattern)));
        }
        if (type != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        if (categoryId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        if (startDate != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
        if (endDate != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
        return transactionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        AppUser user = userContext.currentUser();
        Category category = findCategory(request.categoryId(), user.getId());
        validateCategoryType(category, request.type());
        ExpenseTransaction transaction = new ExpenseTransaction(request.title().trim(), request.amount(),
                request.type(), category, request.transactionDate(), cleanNotes(request.notes()), user);
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        AppUser user = userContext.currentUser();
        ExpenseTransaction transaction = findOwned(id);
        Category category = findCategory(request.categoryId(), user.getId());
        validateCategoryType(category, request.type());
        transaction.update(request.title().trim(), request.amount(), request.type(), category,
                request.transactionDate(), cleanNotes(request.notes()));
        return toResponse(transaction);
    }

    @Transactional
    public void delete(Long id) {
        transactionRepository.delete(findOwned(id));
    }

    private ExpenseTransaction findOwned(Long id) {
        return transactionRepository.findByIdAndUserId(id, userContext.currentUser().getId())
                .orElseThrow(() -> new NotFoundException("Transaction not found."));
    }

    private Category findCategory(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private void validateCategoryType(Category category, TransactionType type) {
        if (category.getType() != CategoryType.BOTH
                && !category.getType().name().equals(type.name())) {
            throw new ConflictException("Select a category that matches the transaction type.");
        }
    }

    private String cleanNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    public TransactionResponse toResponse(ExpenseTransaction transaction) {
        Category category = transaction.getCategory();
        return new TransactionResponse(transaction.getId(), transaction.getTitle(), transaction.getAmount(),
                transaction.getType(), category.getId(), category.getName(), category.getColor(),
                transaction.getTransactionDate(), transaction.getNotes());
    }
}
