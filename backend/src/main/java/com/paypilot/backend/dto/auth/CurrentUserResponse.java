package com.paypilot.backend.dto.auth;

import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.user.UserStatus;

public record CurrentUserResponse(
        Long id,
        Long merchantId,
        String email,
        String displayName,
        UserRole role,
        UserStatus status
) {
}