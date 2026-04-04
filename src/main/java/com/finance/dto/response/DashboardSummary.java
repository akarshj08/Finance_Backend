package com.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<CategoryTotal> categoryTotals;
    private List<MonthlyTrend> monthlyTrends;
    private List<TransactionResponse> recentTransactions;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryTotal {
        private String categoryName;
        private String type;
        private BigDecimal total;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyTrend {
        private int year;
        private int month;
        private String type;
        private BigDecimal total;
    }
}
