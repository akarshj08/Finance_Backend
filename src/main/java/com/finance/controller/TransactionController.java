package com.finance.controller;

import com.finance.domain.transaction.TransactionType;
import com.finance.dto.request.TransactionRequest;
import com.finance.dto.response.PagedResponse;
import com.finance.dto.response.TransactionResponse;
import com.finance.security.CustomUserDetails;
import com.finance.service.TransactionService;
import com.finance.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions with optional filters (all roles)")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getAll(type, categoryId, from, to, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by ID (all roles)")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Create a transaction (Analyst, Admin)")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        TransactionResponse created = transactionService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transaction created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Update a transaction (Analyst, Admin)")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transaction updated", transactionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Soft-delete a transaction (Analyst, Admin)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted"));
    }
}
