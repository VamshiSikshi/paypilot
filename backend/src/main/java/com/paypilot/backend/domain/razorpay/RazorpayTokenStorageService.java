package com.paypilot.backend.domain.razorpay;

public interface RazorpayTokenStorageService {

    String storeTokens(
            Long merchantId,
            String accessToken,
            String refreshToken
    );

    String retrieveAccessToken(String tokenReference);

    String retrieveRefreshToken(String tokenReference);
}
