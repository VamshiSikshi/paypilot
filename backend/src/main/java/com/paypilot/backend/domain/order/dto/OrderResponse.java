package com.paypilot.backend.domain.order.dto;

import com.paypilot.backend.domain.order.Order;
import com.paypilot.backend.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long merchantId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        OrderStatus status,
        String receipt,
        Instant createdAt,
        Instant updatedAt,
        Instant lastSyncedAt,
        BigDecimal amountPaid,
        BigDecimal amountDue,
        Integer attempts,
        String notes
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getMerchant().getId(),
                order.getRazorpayOrderId(),
                order.getAmount(),
                order.getCurrency(),
                order.getStatus(),
                order.getReceipt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getLastSyncedAt(),
                order.getAmountPaid(),
                order.getAmountDue(),
                order.getAttempts(),
                order.getNotes()
        );
    }
}