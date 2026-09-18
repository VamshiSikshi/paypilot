package com.paypilot.backend.domain.user.dto;

import com.paypilot.backend.domain.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull
        UserRole role
) {
}