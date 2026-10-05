package com.paypilot.backend.domain.action;

import com.paypilot.backend.domain.action.dto.ActionExecutionResponse;

public interface ActionExecutionService {

    ActionExecutionResponse createExecution(
            Long merchantId,
            Long userId,
            Long conversationId,
            Long messageId,
            ActionType actionType,
            ActionTargetType targetType,
            Long targetId,
            Boolean confirmationRequired,
            String requestData,
            String idempotencyKey
    );

    ActionExecutionResponse getExecution(
            Long merchantId,
            Long executionId
    );

    ActionExecutionResponse getExecutionByIdempotencyKey(
            Long merchantId,
            String idempotencyKey
    );

    ActionExecutionResponse confirmExecution(
            Long merchantId,
            Long executionId
    );

    ActionExecutionResponse startExecution(
            Long merchantId,
            Long executionId
    );

    ActionExecutionResponse completeExecution(
            Long merchantId,
            Long executionId,
            String resultData
    );

    ActionExecutionResponse failExecution(
            Long merchantId,
            Long executionId,
            String errorMessage
    );

    ActionExecutionResponse cancelExecution(
            Long merchantId,
            Long executionId
    );
}