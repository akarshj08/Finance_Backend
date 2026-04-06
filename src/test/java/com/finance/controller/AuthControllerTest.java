package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.Entity.user.Role;
import com.finance.Configuration.SecurityConfig;
import com.finance.dto.request.LoginRequest;
import com.finance.dto.response.AuthResponse;
import com.finance.security.CustomUserDetailsService;
import com.finance.security.JwtTokenProvider;
import com.finance.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService              authService;
    @MockBean JwtTokenProvider         jwtTokenProvider;
    @MockBean CustomUserDetailsService customUserDetailsService;

    // ─── SUCCESS CASES ───────────────────────────────────────────────────────

    @Test
    void login_shouldReturn200_withTokenAndAdminRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("password");

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .tokenType("Bearer")
                .userId(1L)
                .email("admin@finance.com")
                .fullName("System Admin")
                .role(Role.ADMIN)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.fullName").value("System Admin"))
                .andExpect(jsonPath("$.data.email").value("admin@finance.com"));
    }

    @Test
    void login_shouldReturn200_withAnalystRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("analyst@finance.com");
        request.setPassword("password");

        AuthResponse authResponse = AuthResponse.builder()
                .token("analyst.jwt.token")
                .tokenType("Bearer")
                .userId(2L)
                .email("analyst@finance.com")
                .fullName("Finance Analyst")
                .role(Role.ANALYST)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ANALYST"))
                .andExpect(jsonPath("$.data.fullName").value("Finance Analyst"));
    }

    @Test
    void login_shouldReturn200_withViewerRole() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("viewer@finance.com");
        request.setPassword("password");

        AuthResponse authResponse = AuthResponse.builder()
                .token("viewer.jwt.token")
                .tokenType("Bearer")
                .userId(3L)
                .email("viewer@finance.com")
                .fullName("Dashboard Viewer")
                .role(Role.VIEWER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("VIEWER"));
    }

    // ─── VALIDATION FAILURES ─────────────────────────────────────────────────

    @Test
    void login_shouldReturn400_whenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldReturn400_whenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldReturn400_whenEmailIsInvalidFormat() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldReturn400_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── AUTH FAILURES ───────────────────────────────────────────────────────

    @Test
    void login_shouldReturn401_whenCredentialsAreWrong() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("wrongpassword");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_shouldReturn401_whenAccountIsInactive() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("inactive@finance.com");
        request.setPassword("password");

        when(authService.login(any())).thenThrow(new DisabledException("Account disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Your account is inactive. Contact admin."));
    }
}
