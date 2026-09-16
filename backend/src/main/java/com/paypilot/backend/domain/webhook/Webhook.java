package com.paypilot.backend.domain.webhook;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "webhooks")
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 255)
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WebhookStatus status;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Webhook() {
    }

    public Webhook(
            Merchant merchant,
            String eventId,
            String eventType,
            String payload,
            String signature
    ) {
        this.merchant = merchant;
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.signature = signature;
        this.status = WebhookStatus.RECEIVED;
        this.attemptCount = 0;

        Instant now = Instant.now();
        this.receivedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getSignature() {
        return signature;
    }

    public WebhookStatus getStatus() {
        return status;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}