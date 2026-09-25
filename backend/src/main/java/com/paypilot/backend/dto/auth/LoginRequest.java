package com.paypilot.backend.dto.auth;

public record LoginRequest(
        String merchantAccountId,
        String email,
        String password
) {
}