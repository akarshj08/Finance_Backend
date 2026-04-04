package com.finance.service;

import com.finance.domain.transaction.TransactionType;
import com.finance.dto.response.DashboardSummary;
import com.finance.dto.response.TransactionResponse;
import com.finance.repository.TransactionRepository;
import com.finance.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        List<DashboardSummary.CategoryTotal> categoryTotals = transactionRepository
                .categoryWiseTotals()
                .stream()
                .map(row -> DashboardSummary.CategoryTotal.builder()
                        .categoryName((String) row[0])
                        .type((String) row[1])
                        .total((BigDecimal) row[2])
                        .build())
                .toList();

        // last 6 months of monthly trends
        List<DashboardSummary.MonthlyTrend> monthlyTrends = transactionRepository
                .monthlyTrend(DateRangeUtil.monthsAgo(6))
                .stream()
                .map(row -> DashboardSummary.MonthlyTrend.builder()
                        .year(((Number) row[0]).intValue())
                        .month(((Number) row[1]).intValue())
                        .type(row[2].toString())
                        .total((BigDecimal) row[3])
                        .build())
                .toList();

        List<TransactionResponse> recent = transactionRepository
                .findRecentTransactions(PageRequest.of(0, 10))
                .stream()
                .map(TransactionResponse::from)
                .toList();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .monthlyTrends(monthlyTrends)
                .recentTransactions(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummaryForDateRange(java.time.LocalDate from, java.time.LocalDate to) {
        BigDecimal totalIncome   = transactionRepository.sumByTypeAndDateRange(TransactionType.INCOME, from, to);
        BigDecimal totalExpenses = transactionRepository.sumByTypeAndDateRange(TransactionType.EXPENSE, from, to);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .build();
    }
}
