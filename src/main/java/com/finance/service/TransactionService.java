package com.finance.service;

import com.finance.Entity.transaction.Category;
import com.finance.Entity.transaction.Transaction;
import com.finance.Entity.transaction.TransactionType;
import com.finance.Entity.user.User;
import com.finance.dto.request.TransactionRequest;
import com.finance.dto.response.PagedResponse;
import com.finance.dto.response.TransactionResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.CategoryRepository;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import com.finance.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getAll(
            TransactionType type,
            Long categoryId,
            LocalDate from,
            LocalDate to,
            Pageable pageable) {

        Page<Transaction> page = transactionRepository
                .findAllFiltered(type, categoryId, from, to, pageable);
        return PagedResponse.from(page.map(TransactionResponse::from));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        return TransactionResponse.from(findActive(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request, CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getId()));

        Category category = resolveCategory(request.getCategoryId());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(user)
                .deleted(false)
                .build();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction transaction = findActive(id);

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());
        transaction.setCategory(resolveCategory(request.getCategoryId()));

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = findActive(id);
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    private Transaction findActive(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (t.isDeleted()) {
            throw new ResourceNotFoundException("Transaction", id);
        }
        return t;
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
}
