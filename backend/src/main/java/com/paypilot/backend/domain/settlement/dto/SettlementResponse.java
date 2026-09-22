package com.paypilot.backend.domain.settlement.dto;

import com.paypilot.backend.domain.settlement.Settlement;
import com.paypilot.backend.domain.settlement.SettlementStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementResponse(
        Long id,
        Long merchantId,
        String razorpaySettlementId,
        BigDecimal amount,
        String currency,
        SettlementStatus status,
        Instant settledAt,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSyncedAt,
        String settlementUtr,
        BigDecimal fee,
        BigDecimal tax
) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getMerchant().getId(),
                settlement.getRazorpaySettlementId(),
                settlement.getAmount(),
                settlement.getCurrency(),
                settlement.getStatus(),
                settlement.getSettledAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt(),
                settlement.getLastSyncedAt(),
                settlement.getSettlementUtr(),
                settlement.getFee(),
                settlement.getTax()
        );
    }
}