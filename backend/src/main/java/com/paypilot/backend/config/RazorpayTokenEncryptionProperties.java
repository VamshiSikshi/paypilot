package com.paypilot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypilot.razorpay.token-encryption")
public record RazorpayTokenEncryptionProperties(
        String keyBase64,
        String keyVersion
) {
}