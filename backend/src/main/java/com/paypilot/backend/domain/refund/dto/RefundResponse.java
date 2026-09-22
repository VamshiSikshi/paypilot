package com.paypilot.backend.domain.refund.dto;

import com.paypilot.backend.domain.refund.Refund;
import com.paypilot.backend.domain.refund.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(
        Long id,
        Long merchantId,
        Long paymentId,
        String razorpayRefundId,
        String razorpayPaymentId,
        BigDecimal amount,
        String currency,
        RefundStatus status,
        String speedRequested,
        String speedProcessed,
        String notes,
        String failureReason,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSyncedAt
) {

    public static RefundResponse from(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getMerchant().getId(),
                refund.getPayment().getId(),
                refund.getRazorpayRefundId(),
                refund.getRazorpayPaymentId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getSpeedRequested(),
                refund.getSpeedProcessed(),
                refund.getNotes(),
                refund.getFailureReason(),
                refund.getCreatedAt(),
                refund.getUpdatedAt(),
                refund.getLastSyncedAt()
        );
    }
}