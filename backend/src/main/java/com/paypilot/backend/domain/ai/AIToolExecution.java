package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_tool_executions")
public class AIToolExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private AIConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private AIMessage message;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "tool_call_id", nullable = false, unique = true, length = 100)
    private String toolCallId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AIToolExecutionStatus status;

    @Column
    private String input;

    @Column
    private String output;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AIToolExecution() {
    }

    public AIToolExecution(
            Merchant merchant,
            AIConversation conversation,
            AIMessage message,
            String toolName,
            String toolCallId,
            String input
    ) {
        this.merchant = merchant;
        this.conversation = conversation;
        this.message = message;
        this.toolName = toolName;
        this.toolCallId = toolCallId;
        this.status = AIToolExecutionStatus.PENDING;
        this.input = input;

        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public AIConversation getConversation() {
        return conversation;
    }

    public AIMessage getMessage() {
        return message;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public AIToolExecutionStatus getStatus() {
        return status;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}