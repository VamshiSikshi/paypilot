package com.paypilot.backend.domain.action;

import com.paypilot.backend.domain.ai.AIConversation;
import com.paypilot.backend.domain.ai.AIMessage;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "action_executions")
public class ActionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private AIConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private AIMessage message;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ActionTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionExecutionStatus status;

    @Column(name = "confirmation_required", nullable = false)
    private Boolean confirmationRequired;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "request_data")
    private String requestData;

    @Column(name = "result_data")
    private String resultData;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    protected ActionExecution() {
    }

    public ActionExecution(
            Merchant merchant,
            User user,
            AIConversation conversation,
            AIMessage message,
            ActionType actionType,
            ActionTargetType targetType,
            Long targetId,
            Boolean confirmationRequired,
            String requestData,
            String idempotencyKey
    ) {
        this.merchant = merchant;
        this.user = user;
        this.conversation = conversation;
        this.message = message;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = confirmationRequired
                ? ActionExecutionStatus.CONFIRMATION_REQUIRED
                : ActionExecutionStatus.PENDING;
        this.confirmationRequired = confirmationRequired;
        this.requestedAt = Instant.now();
        this.requestData = requestData;
        this.idempotencyKey = idempotencyKey;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public User getUser() {
        return user;
    }

    public AIConversation getConversation() {
        return conversation;
    }

    public AIMessage getMessage() {
        return message;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public ActionTargetType getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public ActionExecutionStatus getStatus() {
        return status;
    }

    public Boolean getConfirmationRequired() {
        return confirmationRequired;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getRequestData() {
        return requestData;
    }

    public String getResultData() {
        return resultData;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}