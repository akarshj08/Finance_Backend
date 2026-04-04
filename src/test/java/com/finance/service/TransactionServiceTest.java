package com.finance.service;

import com.finance.domain.transaction.Category;
import com.finance.domain.transaction.Transaction;
import com.finance.domain.transaction.TransactionType;
import com.finance.domain.user.Role;
import com.finance.domain.user.User;
import com.finance.domain.user.UserStatus;
import com.finance.dto.request.TransactionRequest;
import com.finance.dto.response.TransactionResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.CategoryRepository;
import com.finance.repository.TransactionRepository;
import com.finance.repository.UserRepository;
import com.finance.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock CategoryRepository    categoryRepository;
    @Mock UserRepository        userRepository;

    @InjectMocks TransactionService transactionService;

    private User user;
    private CustomUserDetails userDetails;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).fullName("Test User").email("test@test.com")
                .password("encoded").role(Role.ANALYST).status(UserStatus.ACTIVE)
                .build();

        userDetails = new CustomUserDetails(user);

        category = Category.builder().id(1L).name("Salary").build();
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(5000));
        req.setType(TransactionType.INCOME);
        req.setCategoryId(1L);
        req.setDate(LocalDate.now());
        req.setNotes("Monthly salary");

        Transaction saved = Transaction.builder()
                .id(1L).amount(req.getAmount()).type(req.getType())
                .category(category).date(req.getDate()).notes(req.getNotes())
                .createdBy(user).deleted(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.create(req, userDetails);

        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getCategoryName()).isEqualTo("Salary");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getById_shouldThrow_whenDeleted() {
        Transaction deleted = Transaction.builder().id(99L).deleted(true).build();
        when(transactionRepository.findById(99L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> transactionService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldMarkAsDeleted() {
        Transaction t = Transaction.builder().id(1L).deleted(false).createdBy(user).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));
        when(transactionRepository.save(any())).thenReturn(t);

        transactionService.delete(1L);

        assertThat(t.isDeleted()).isTrue();
        verify(transactionRepository).save(t);
    }

    @Test
    void create_shouldThrow_whenCategoryNotFound() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(100));
        req.setType(TransactionType.EXPENSE);
        req.setCategoryId(999L);
        req.setDate(LocalDate.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(req, userDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
