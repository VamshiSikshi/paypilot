package com.paypilot.backend.domain.notification;

public enum NotificationType {

    PAYMENT_FAILED,
    PAYMENT_SUCCESS,
    REFUND_COMPLETED,
    REFUND_FAILED,
    SETTLEMENT_PROCESSED,
    SETTLEMENT_FAILED,
    SYNC_COMPLETED,
    SYNC_FAILED,
    AI_INSIGHT,
    SYSTEM
}