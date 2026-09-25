package com.paypilot.backend.domain.ai.dto;

import com.paypilot.backend.domain.ai.AIConversationStatus;

import java.time.Instant;

public record AIConversationResponse(
        Long id,
        Long merchantId,
        Long userId,
        String title,
        AIConversationStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant lastMessageAt
) {
}