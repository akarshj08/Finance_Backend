package com.finance.controller;

import com.finance.dto.response.DashboardSummary;
import com.finance.service.DashboardService;
import com.finance.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    @Operation(summary = "Get full dashboard summary (all roles)")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/summary/range")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    @Operation(summary = "Get summary for a custom date range (Analyst, Admin)")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummaryForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummaryForDateRange(from, to)));
    }
}
