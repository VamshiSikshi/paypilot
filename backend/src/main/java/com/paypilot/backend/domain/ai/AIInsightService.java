package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.ai.dto.AIInsightResponse;

import java.time.Instant;
import java.util.List;

public interface AIInsightService {

    AIInsightResponse createInsight(
            Long merchantId,
            String insightType,
            InsightSeverity severity,
            String title,
            String description,
            InsightSource source,
            String data,
            Instant validFrom,
            Instant validUntil
    );

    AIInsightResponse getInsight(
            Long merchantId,
            Long insightId
    );

    List<AIInsightResponse> getInsights(
            Long merchantId
    );
}