package com.paypilot.backend.domain.razorpay;

import java.time.Instant;

public interface RazorpayConnectionService {

    RazorpayConnection getConnection(Long merchantId);

    RazorpayConnection disconnect(Long merchantId);

    RazorpayConnection recordSuccessfulSync(
            Long merchantId,
            Instant syncedAt
    );
}
