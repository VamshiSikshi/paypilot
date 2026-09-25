package com.paypilot.backend.security;

import com.paypilot.backend.domain.user.UserRole;

public record AuthenticatedUser(
        Long userId,
        Long merchantId,
        UserRole role
) {
}