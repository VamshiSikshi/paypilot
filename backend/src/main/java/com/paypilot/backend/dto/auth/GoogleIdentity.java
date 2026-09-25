package com.paypilot.backend.dto.auth;

public record GoogleIdentity(
        String subject,
        String email,
        boolean emailVerified,
        String displayName
) {
}