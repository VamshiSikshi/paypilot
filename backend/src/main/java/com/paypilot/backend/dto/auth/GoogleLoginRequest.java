package com.paypilot.backend.dto.auth;

public record GoogleLoginRequest(
        String merchantAccountId,
        String idToken
) {
}