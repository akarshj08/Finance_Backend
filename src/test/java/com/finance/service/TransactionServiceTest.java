package com.finance.service;

import com.finance.Entity.transaction.Category;
import com.finance.Entity.transaction.Transaction;
import com.finance.Entity.transaction.TransactionType;
import com.finance.Entity.user.Role;
import com.finance.Entity.user.User;
import com.finance.Entity.user.UserStatus;
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

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSave_forIncome() {
        TransactionRequest req = req(BigDecimal.valueOf(5000), TransactionType.INCOME, 1L, "Monthly salary");
        Transaction saved = tx(1L, req, category, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse res = transactionService.create(req, userDetails);

        assertThat(res.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(res.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(res.getCategoryName()).isEqualTo("Salary");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void create_shouldSave_forExpense() {
        Category food = Category.builder().id(2L).name("Food").build();
        TransactionRequest req = req(BigDecimal.valueOf(1500), TransactionType.EXPENSE, 2L, "Groceries");
        Transaction saved = tx(2L, req, food, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(food));
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse res = transactionService.create(req, userDetails);

        assertThat(res.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(res.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void create_shouldSave_forInvestment() {
        Category invest = Category.builder().id(3L).name("Investment").build();
        TransactionRequest req = req(BigDecimal.valueOf(20000), TransactionType.INVESTMENT, 3L, "Stock market");
        Transaction saved = tx(3L, req, invest, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(invest));
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse res = transactionService.create(req, userDetails);

        assertThat(res.getType()).isEqualTo(TransactionType.INVESTMENT);
        assertThat(res.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(res.getCategoryName()).isEqualTo("Investment");
    }

    @Test
    void create_shouldSaveWithoutCategory_whenCategoryIdNull() {
        TransactionRequest req = req(BigDecimal.valueOf(3000), TransactionType.INCOME, null, "Cash");
        Transaction saved = tx(4L, req, null, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse res = transactionService.create(req, userDetails);

        assertThat(res.getCategoryName()).isNull();
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrow_whenCategoryNotFound() {
        TransactionRequest req = req(BigDecimal.valueOf(100), TransactionType.EXPENSE, 999L, "test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(req, userDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        TransactionRequest req = req(BigDecimal.valueOf(100), TransactionType.INCOME, null, "test");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(req, userDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── GET ─────────────────────────────────────────────────────────────────

    @Test
    void getById_shouldReturn_whenActive() {
        Transaction t = tx(1L, req(BigDecimal.valueOf(5000), TransactionType.INCOME, null, "salary"), null, user);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));

        TransactionResponse res = transactionService.getById(1L);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    void getById_shouldThrow_whenDeleted() {
        Transaction deleted = Transaction.builder().id(99L).deleted(true).build();
        when(transactionRepository.findById(99L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> transactionService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateAllFields() {
        Transaction existing = tx(1L, req(BigDecimal.valueOf(5000), TransactionType.INCOME, null, "old"), null, user);
        TransactionRequest updateReq = req(BigDecimal.valueOf(7000), TransactionType.INVESTMENT, null, "updated");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(existing);

        transactionService.update(1L, updateReq);

        assertThat(existing.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(7000));
        assertThat(existing.getType()).isEqualTo(TransactionType.INVESTMENT);
        assertThat(existing.getNotes()).isEqualTo("updated");
        verify(transactionRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenDeleted() {
        Transaction deleted = Transaction.builder().id(1L).deleted(true).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> transactionService.update(1L,
                req(BigDecimal.valueOf(100), TransactionType.INCOME, null, "test")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

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
    void delete_shouldThrow_whenAlreadyDeleted() {
        Transaction deleted = Transaction.builder().id(1L).deleted(true).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> transactionService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private TransactionRequest req(BigDecimal amount, TransactionType type, Long catId, String notes) {
        TransactionRequest r = new TransactionRequest();
        r.setAmount(amount);
        r.setType(type);
        r.setCategoryId(catId);
        r.setDate(LocalDate.now());
        r.setNotes(notes);
        return r;
    }

    private Transaction tx(Long id, TransactionRequest req, Category cat, User createdBy) {
        return Transaction.builder()
                .id(id).amount(req.getAmount()).type(req.getType())
                .category(cat).date(req.getDate()).notes(req.getNotes())
                .createdBy(createdBy).deleted(false).build();
    }
}
