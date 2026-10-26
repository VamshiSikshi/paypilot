package com.paypilot.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypilot.razorpay.oauth")
public record RazorpayOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String scope,
        String authorizationUri,
        String tokenUri,
        String mode
) {
}