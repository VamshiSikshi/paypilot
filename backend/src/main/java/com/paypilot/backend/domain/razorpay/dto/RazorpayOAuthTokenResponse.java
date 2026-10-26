package com.paypilot.backend.domain.razorpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RazorpayOAuthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("razorpay_account_id") String razorpayAccountId,
        @JsonProperty("scope") String scope,
        @JsonProperty("token_type") String tokenType
) {
}
