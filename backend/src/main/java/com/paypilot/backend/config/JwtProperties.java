package com.paypilot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypilot.security.jwt")
public record JwtProperties(
        String secret,
        long expiration,
        String issuer
) {
}