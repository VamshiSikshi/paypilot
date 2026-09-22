package com.paypilot.backend.domain.payment.dto;

import com.paypilot.backend.domain.payment.Payment;
import com.paypilot.backend.domain.payment.PaymentStatus;
import com.paypilot.backend.domain.payment.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long merchantId,
        String razorpayPaymentId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String method,
        Instant capturedAt,
        String customerEmail,
        String customerContact,
        String errorCode,
        String errorDescription,
        String errorSource,
        String errorReason,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSyncedAt,
        Long orderId,
        BigDecimal fee,
        BigDecimal tax,
        BigDecimal amountRefunded,
        RefundStatus refundStatus,
        Boolean captured,
        Boolean international,
        String description,
        String notes
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchant().getId(),
                payment.getRazorpayPaymentId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getCapturedAt(),
                payment.getCustomerEmail(),
                payment.getCustomerContact(),
                payment.getErrorCode(),
                payment.getErrorDescription(),
                payment.getErrorSource(),
                payment.getErrorReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getLastSyncedAt(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getFee(),
                payment.getTax(),
                payment.getAmountRefunded(),
                payment.getRefundStatus(),
                payment.getCaptured(),
                payment.getInternational(),
                payment.getDescription(),
                payment.getNotes()
        );
    }
}