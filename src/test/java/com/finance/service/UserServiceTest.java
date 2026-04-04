package com.finance.service;

import com.finance.domain.user.Role;
import com.finance.domain.user.User;
import com.finance.domain.user.UserStatus;
import com.finance.dto.request.CreateUserRequest;
import com.finance.dto.response.UserResponse;
import com.finance.exception.ValidationException;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository  userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    @Test
    void createUser_shouldSucceed() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFullName("Jane Doe");
        req.setEmail("jane@example.com");
        req.setPassword("password123");
        req.setRole(Role.ANALYST);

        User saved = User.builder()
                .id(1L).fullName("Jane Doe").email("jane@example.com")
                .password("encoded").role(Role.ANALYST).status(UserStatus.ACTIVE)
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = userService.createUser(req);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ANALYST);
    }

    @Test
    void createUser_shouldThrow_whenEmailExists() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already in use");
    }
}
