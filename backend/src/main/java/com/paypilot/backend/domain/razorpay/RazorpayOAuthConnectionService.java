package com.paypilot.backend.domain.razorpay;

import java.time.Instant;

public interface RazorpayOAuthConnectionService {

    RazorpayConnection completeAuthorization(
            Long merchantId,
            String externalAccountId,
            String accessToken,
            String refreshToken,
            Instant tokenExpiresAt,
            String grantedScopes
    );
}
