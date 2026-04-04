package com.finance.controller;

import com.finance.dto.request.LoginRequest;
import com.finance.dto.response.AuthResponse;
import com.finance.service.AuthService;
import com.finance.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController
{

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }
}
