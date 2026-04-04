package com.finance.dto.request;

import com.finance.domain.user.Role;
import com.finance.domain.user.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 100)
    private String fullName;
    private Role role;
    private UserStatus status;
}
