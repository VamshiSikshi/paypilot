package com.paypilot.backend.domain.action.dto;

import com.paypilot.backend.domain.action.ActionExecution;
import com.paypilot.backend.domain.action.ActionExecutionStatus;
import com.paypilot.backend.domain.action.ActionTargetType;
import com.paypilot.backend.domain.action.ActionType;

import java.time.Instant;

public record ActionExecutionResponse(
        Long id,
        Long merchantId,
        Long userId,
        Long conversationId,
        Long messageId,
        ActionType actionType,
        ActionTargetType targetType,
        Long targetId,
        ActionExecutionStatus status,
        Boolean confirmationRequired,
        Instant confirmedAt,
        Instant requestedAt,
        Instant startedAt,
        Instant completedAt,
        String requestData,
        String resultData,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        String idempotencyKey
) {

    public static ActionExecutionResponse from(
            ActionExecution execution
    ) {
        return new ActionExecutionResponse(
                execution.getId(),
                execution.getMerchant().getId(),
                execution.getUser().getId(),
                execution.getConversation() != null
                        ? execution.getConversation().getId()
                        : null,
                execution.getMessage() != null
                        ? execution.getMessage().getId()
                        : null,
                execution.getActionType(),
                execution.getTargetType(),
                execution.getTargetId(),
                execution.getStatus(),
                execution.getConfirmationRequired(),
                execution.getConfirmedAt(),
                execution.getRequestedAt(),
                execution.getStartedAt(),
                execution.getCompletedAt(),
                execution.getRequestData(),
                execution.getResultData(),
                execution.getErrorMessage(),
                execution.getCreatedAt(),
                execution.getUpdatedAt(),
                execution.getIdempotencyKey()
        );
    }
}