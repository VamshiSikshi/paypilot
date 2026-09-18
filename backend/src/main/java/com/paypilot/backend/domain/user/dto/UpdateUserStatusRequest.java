package com.paypilot.backend.domain.user.dto;

import com.paypilot.backend.domain.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull
        UserStatus status
) {
}