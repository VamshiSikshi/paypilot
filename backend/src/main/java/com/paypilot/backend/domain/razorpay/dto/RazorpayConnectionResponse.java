package com.paypilot.backend.domain.razorpay.dto;

import com.paypilot.backend.domain.razorpay.RazorpayConnection;
import com.paypilot.backend.domain.razorpay.RazorpayConnectionStatus;

import java.time.Instant;

public record RazorpayConnectionResponse(
        Long id,
        String externalAccountId,
        RazorpayConnectionStatus status,
        Instant tokenExpiresAt,
        String grantedScopes,
        Instant lastConnectedAt,
        Instant lastSyncedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static RazorpayConnectionResponse from(
            RazorpayConnection connection
    ) {
        return new RazorpayConnectionResponse(
                connection.getId(),
                connection.getExternalAccountId(),
                connection.getStatus(),
                connection.getTokenExpiresAt(),
                connection.getGrantedScopes(),
                connection.getLastConnectedAt(),
                connection.getLastSyncedAt(),
                connection.getCreatedAt(),
                connection.getUpdatedAt()
        );
    }
}