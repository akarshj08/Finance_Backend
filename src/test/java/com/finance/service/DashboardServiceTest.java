package com.finance.service;

import com.finance.Entity.transaction.TransactionType;
import com.finance.dto.response.DashboardSummary;
import com.finance.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest
{

    @Mock TransactionRepository transactionRepository;
    @InjectMocks DashboardService dashboardService;

    // ─── SUMMARY TESTS ───────────────────────────────────────────────────────

    @Test
    void getSummary_shouldReturnCorrectTotals()
    {
        when(transactionRepository.sumByType(TransactionType.INCOME)).thenReturn(BigDecimal.valueOf(100000));
        when(transactionRepository.sumByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.valueOf(30000));
        when(transactionRepository.sumByType(TransactionType.INVESTMENT)).thenReturn(BigDecimal.valueOf(20000));
        when(transactionRepository.categoryWiseTotals()).thenReturn(Collections.emptyList());
        when(transactionRepository.monthlyTrend(any())).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());
        DashboardSummary summary = dashboardService.getSummary();
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(summary.getTotalInvestments()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    void getSummary_shouldCalculateNetBalance_withAllZeroExpenseAndInvestment()
    {
        when(transactionRepository.sumByType(TransactionType.INCOME)).thenReturn(BigDecimal.valueOf(50000));
        when(transactionRepository.sumByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByType(TransactionType.INVESTMENT)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.categoryWiseTotals()).thenReturn(Collections.emptyList());
        when(transactionRepository.monthlyTrend(any())).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());
        DashboardSummary summary = dashboardService.getSummary();
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    void getSummary_shouldReturnNegativeBalance_whenExpensesExceedIncome()
    {
        when(transactionRepository.sumByType(TransactionType.INCOME)).thenReturn(BigDecimal.valueOf(10000));
        when(transactionRepository.sumByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.valueOf(15000));
        when(transactionRepository.sumByType(TransactionType.INVESTMENT)).thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepository.categoryWiseTotals()).thenReturn(Collections.emptyList());
        when(transactionRepository.monthlyTrend(any())).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());
        DashboardSummary summary = dashboardService.getSummary();
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.valueOf(-10000));
    }

    @Test
    void getSummary_shouldReturnEmptyLists_whenNoTransactions() {
        when(transactionRepository.sumByType(any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.categoryWiseTotals()).thenReturn(Collections.emptyList());
        when(transactionRepository.monthlyTrend(any())).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());

        DashboardSummary summary = dashboardService.getSummary();

        assertThat(summary.getCategoryTotals()).isEmpty();
        assertThat(summary.getMonthlyTrends()).isEmpty();
        assertThat(summary.getRecentTransactions()).isEmpty();
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getSummary_shouldCallSumByType_forAllThreeTypes() {
        when(transactionRepository.sumByType(any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.categoryWiseTotals()).thenReturn(Collections.emptyList());
        when(transactionRepository.monthlyTrend(any())).thenReturn(Collections.emptyList());
        when(transactionRepository.findRecentTransactions(any(Pageable.class))).thenReturn(Collections.emptyList());

        dashboardService.getSummary();

        verify(transactionRepository).sumByType(TransactionType.INCOME);
        verify(transactionRepository).sumByType(TransactionType.EXPENSE);
        verify(transactionRepository).sumByType(TransactionType.INVESTMENT);
    }

    // ─── DATE RANGE SUMMARY TESTS ────────────────────────────────────────────

    @Test
    void getSummaryForDateRange_shouldReturnCorrectTotals() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to   = LocalDate.of(2026, 3, 31);

        when(transactionRepository.sumByTypeAndDateRange(TransactionType.INCOME, from, to))
                .thenReturn(BigDecimal.valueOf(80000));
        when(transactionRepository.sumByTypeAndDateRange(TransactionType.EXPENSE, from, to))
                .thenReturn(BigDecimal.valueOf(20000));
        when(transactionRepository.sumByTypeAndDateRange(TransactionType.INVESTMENT, from, to))
                .thenReturn(BigDecimal.valueOf(15000));

        DashboardSummary summary = dashboardService.getSummaryForDateRange(from, to);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.valueOf(80000));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(summary.getTotalInvestments()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.valueOf(45000));
    }

    @Test
    void getSummaryForDateRange_shouldCallRepositoryWithCorrectDates() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to   = LocalDate.of(2026, 12, 31);

        when(transactionRepository.sumByTypeAndDateRange(any(), eq(from), eq(to)))
                .thenReturn(BigDecimal.ZERO);

        dashboardService.getSummaryForDateRange(from, to);

        verify(transactionRepository).sumByTypeAndDateRange(TransactionType.INCOME, from, to);
        verify(transactionRepository).sumByTypeAndDateRange(TransactionType.EXPENSE, from, to);
        verify(transactionRepository).sumByTypeAndDateRange(TransactionType.INVESTMENT, from, to);
    }
}
