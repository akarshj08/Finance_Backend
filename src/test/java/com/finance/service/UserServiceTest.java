package com.finance.service;

import com.finance.Entity.user.Role;
import com.finance.Entity.user.User;
import com.finance.Entity.user.UserStatus;
import com.finance.dto.request.CreateUserRequest;
import com.finance.dto.request.UpdateUserRequest;
import com.finance.dto.response.UserResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.exception.ValidationException;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository  userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Test
    void createUser_shouldSucceed_withAnalystRole() {
        CreateUserRequest req = createReq("Jane Doe", "jane@example.com", "password123", Role.ANALYST);

        User saved = user(1L, "Jane Doe", "jane@example.com", Role.ANALYST, UserStatus.ACTIVE);

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = userService.createUser(req);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ANALYST);
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldSucceed_withViewerRole() {
        CreateUserRequest req = createReq("John Viewer", "viewer@example.com", "pass1234", Role.VIEWER);

        User saved = user(2L, "John Viewer", "viewer@example.com", Role.VIEWER, UserStatus.ACTIVE);

        when(userRepository.existsByEmail("viewer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = userService.createUser(req);

        assertThat(response.getRole()).isEqualTo(Role.VIEWER);
    }

    @Test
    void createUser_shouldSucceed_withAdminRole() {
        CreateUserRequest req = createReq("Super Admin", "admin2@example.com", "admin1234", Role.ADMIN);

        User saved = user(3L, "Super Admin", "admin2@example.com", Role.ADMIN, UserStatus.ACTIVE);

        when(userRepository.existsByEmail("admin2@example.com")).thenReturn(false);
        when(passwordEncoder.encode("admin1234")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = userService.createUser(req);

        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        CreateUserRequest req = createReq("Jane Doe", "existing@example.com", "pass1234", Role.ANALYST);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldEncodePassword() {
        CreateUserRequest req = createReq("Jane", "jane@example.com", "rawPassword", Role.VIEWER);
        User saved = user(1L, "Jane", "jane@example.com", Role.VIEWER, UserStatus.ACTIVE);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(saved);

        userService.createUser(req);

        verify(passwordEncoder).encode("rawPassword");
    }

    // ─── GET ─────────────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturn_whenExists() {
        User u = user(1L, "Jane", "jane@example.com", Role.ANALYST, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Test
    void updateUser_shouldUpdateFullName() {
        User existing = user(1L, "Old Name", "jane@example.com", Role.ANALYST, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenReturn(existing);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("New Name");

        userService.updateUser(1L, req);

        assertThat(existing.getFullName()).isEqualTo("New Name");
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_shouldUpdateRole() {
        User existing = user(1L, "Jane", "jane@example.com", Role.VIEWER, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenReturn(existing);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setRole(Role.ANALYST);

        userService.updateUser(1L, req);

        assertThat(existing.getRole()).isEqualTo(Role.ANALYST);
    }

    @Test
    void updateUser_shouldUpdateStatus() {
        User existing = user(1L, "Jane", "jane@example.com", Role.ANALYST, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenReturn(existing);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setStatus(UserStatus.INACTIVE);

        userService.updateUser(1L, req);

        assertThat(existing.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    // ─── DEACTIVATE ──────────────────────────────────────────────────────────

    @Test
    void deactivateUser_shouldSetStatusInactive() {
        User existing = user(1L, "Jane", "jane@example.com", Role.ANALYST, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenReturn(existing);

        userService.deactivateUser(1L);

        assertThat(existing.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(existing);
    }

    @Test
    void deactivateUser_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private CreateUserRequest createReq(String name, String email, String password, Role role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setFullName(name);
        req.setEmail(email);
        req.setPassword(password);
        req.setRole(role);
        return req;
    }

    private User user(Long id, String name, String email, Role role, UserStatus status) {
        return User.builder()
                .id(id).fullName(name).email(email)
                .password("encoded").role(role).status(status)
                .build();
    }
}
