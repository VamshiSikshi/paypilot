package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AIConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private AIMessage message;

    @Column(name = "tool_name", nullable = false, length = 150)
    private String toolName;

    @Column(name = "tool_call_id",unique = true, length = 150)
    private String toolCallId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AIToolExecutionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    private String input;

    @JdbcTypeCode(SqlTypes.JSON)
    private String output;

    @Column(name = "error_message", columnDefinition = "TEXT")
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

    public void start() {
        this.status = AIToolExecutionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete(String output) {
        Instant completedAt = Instant.now();

        this.status = AIToolExecutionStatus.COMPLETED;
        this.output = output;
        this.completedAt = completedAt;

        if (startedAt != null) {
            this.durationMs =
                    completedAt.toEpochMilli() - startedAt.toEpochMilli();
        }
    }

    public void fail(String errorMessage) {
        Instant completedAt = Instant.now();

        this.status = AIToolExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = completedAt;

        if (startedAt != null) {
            this.durationMs =
                    completedAt.toEpochMilli() - startedAt.toEpochMilli();
        }
    }
}