package com.paypilot.backend.domain.ai;

public interface AIToolService {

    AIToolExecution createExecution(
            Long merchantId,
            Long conversationId,
            Long messageId,
            String toolName,
            String toolCallId,
            String input
    );

    AIToolExecution getExecution(
            Long merchantId,
            Long executionId
    );

    AIToolExecution getExecutionByToolCallId(
            Long merchantId,
            String toolCallId
    );

    AIToolExecution startExecution(
            Long merchantId,
            Long executionId
    );

    AIToolExecution completeExecution(
            Long merchantId,
            Long executionId,
            String output
    );

    AIToolExecution failExecution(
            Long merchantId,
            Long executionId,
            String errorMessage
    );
}