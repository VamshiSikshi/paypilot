package com.paypilot.backend.domain.merchant.dto;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantStatus;

import java.time.Instant;

public record MerchantResponse(
        Long id,
        String razorpayAccountId,
        String businessName,
        String country,
        String defaultCurrency,
        String timezone,
        MerchantStatus status,
        Instant lastSyncedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getRazorpayAccountId(),
                merchant.getBusinessName(),
                merchant.getCountry(),
                merchant.getDefaultCurrency(),
                merchant.getTimezone(),
                merchant.getStatus(),
                merchant.getLastSyncedAt(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );
    }
}