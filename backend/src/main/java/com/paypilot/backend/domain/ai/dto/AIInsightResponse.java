package com.paypilot.backend.domain.ai.dto;

import com.paypilot.backend.domain.ai.AIInsight;
import com.paypilot.backend.domain.ai.InsightSeverity;
import com.paypilot.backend.domain.ai.InsightSource;
import com.paypilot.backend.domain.ai.InsightStatus;

import java.time.Instant;

public record AIInsightResponse(
        Long id,
        Long merchantId,
        String insightType,
        InsightSeverity severity,
        String title,
        String description,
        InsightStatus status,
        InsightSource source,
        String data,
        Instant validFrom,
        Instant validUntil,
        Instant createdAt,
        Instant updatedAt
) {
    public static AIInsightResponse from(AIInsight insight) {
        return new AIInsightResponse(
                insight.getId(),
                insight.getMerchant().getId(),
                insight.getInsightType(),
                insight.getSeverity(),
                insight.getTitle(),
                insight.getDescription(),
                insight.getStatus(),
                insight.getSource(),
                insight.getData(),
                insight.getValidFrom(),
                insight.getValidUntil(),
                insight.getCreatedAt(),
                insight.getUpdatedAt()
        );
    }
}