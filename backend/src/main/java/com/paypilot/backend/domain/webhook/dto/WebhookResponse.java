package com.paypilot.backend.domain.webhook.dto;

import com.paypilot.backend.domain.webhook.Webhook;
import com.paypilot.backend.domain.webhook.WebhookStatus;

import java.time.Instant;

public record WebhookResponse(
        Long id,
        Long merchantId,
        String eventId,
        String eventType,
        String payload,
        String signature,
        WebhookStatus status,
        Integer attemptCount,
        Instant receivedAt,
        Instant processedAt,
        Instant lastAttemptAt,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public static WebhookResponse from(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                webhook.getMerchant().getId(),
                webhook.getEventId(),
                webhook.getEventType(),
                webhook.getPayload(),
                webhook.getSignature(),
                webhook.getStatus(),
                webhook.getAttemptCount(),
                webhook.getReceivedAt(),
                webhook.getProcessedAt(),
                webhook.getLastAttemptAt(),
                webhook.getErrorMessage(),
                webhook.getCreatedAt(),
                webhook.getUpdatedAt()
        );
    }
}