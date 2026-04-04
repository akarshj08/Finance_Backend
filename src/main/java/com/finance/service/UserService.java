package com.finance.service;

import com.finance.domain.user.User;
import com.finance.domain.user.UserStatus;
import com.finance.dto.request.CreateUserRequest;
import com.finance.dto.request.UpdateUserRequest;
import com.finance.dto.response.PagedResponse;
import com.finance.dto.response.UserResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.exception.ValidationException;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(UserStatus status, Pageable pageable) {
        Page<User> page = (status != null)
                ? userRepository.findAllByStatus(status, pageable)
                : userRepository.findAll(pageable);
        return PagedResponse.from(page.map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findById(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findById(id);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getRole() != null)     user.setRole(request.getRole());
        if (request.getStatus() != null)   user.setStatus(request.getStatus());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = findById(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
