package com.paypilot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypilot.google")
public record GoogleProperties(
        String clientId
) {
}