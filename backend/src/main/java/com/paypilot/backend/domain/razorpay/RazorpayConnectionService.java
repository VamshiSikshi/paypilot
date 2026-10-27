package com.paypilot.backend.domain.razorpay;

import java.time.Instant;

public interface RazorpayConnectionService {

    RazorpayConnection getConnection(Long merchantId);

    RazorpayConnection disconnect(Long merchantId, Long actorUserId);

    RazorpayConnection recordSuccessfulSync(
            Long merchantId,
            Instant syncedAt
    );

    RazorpayConnection connectAuthorizedAccount(
            Long merchantId,
            String externalAccountId,
            String accessTokenReference,
            String refreshTokenReference,
            Instant tokenExpiresAt,
            String grantedScopes
    );
}