package com.finance.dto.request;

import com.finance.domain.transaction.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    private Long categoryId;

    @NotNull @PastOrPresent
    private LocalDate date;

    @Size(max = 500)
    private String notes;
}
