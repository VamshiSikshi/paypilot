package com.paypilot.backend.domain.user.dto;

import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.user.UserStatus;

import java.time.Instant;

public record UserResponse(
        Long id,
        Long merchantId,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getMerchant().getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}