package com.finance.dto.response;

import com.finance.domain.transaction.Transaction;
import com.finance.domain.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String categoryName;
    private LocalDate date;
    private String notes;
    private String createdByEmail;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .date(t.getDate())
                .notes(t.getNotes())
                .createdByEmail(t.getCreatedBy().getEmail())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
