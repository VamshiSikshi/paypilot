package com.paypilot.backend.domain.action.dto;

import com.paypilot.backend.domain.action.ActionTargetType;
import com.paypilot.backend.domain.action.ActionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateActionExecutionRequest(
        @NotNull
        ActionType actionType,

        @NotNull
        ActionTargetType targetType,

        @NotNull
        @Positive
        Long targetId,

        String requestData,

        @Size(max = 100)
        String idempotencyKey
) {
}
