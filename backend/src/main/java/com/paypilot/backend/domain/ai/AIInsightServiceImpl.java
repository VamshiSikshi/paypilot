package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.ai.dto.AIInsightResponse;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AIInsightServiceImpl implements AIInsightService {

    private final AIInsightRepository insightRepository;
    private final MerchantRepository merchantRepository;

    public AIInsightServiceImpl(
            AIInsightRepository insightRepository,
            MerchantRepository merchantRepository
    ) {
        this.insightRepository = insightRepository;
        this.merchantRepository = merchantRepository;
    }

    @Override
    public AIInsightResponse createInsight(
            Long merchantId,
            String insightType,
            InsightSeverity severity,
            String title,
            String description,
            InsightSource source,
            String data,
            Instant validFrom,
            Instant validUntil
    ) {
        var merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Merchant not found"));

        AIInsight insight = new AIInsight(
                merchant,
                insightType,
                severity,
                title,
                description,
                source,
                data,
                validFrom,
                validUntil
        );

        return AIInsightResponse.from(
                insightRepository.save(insight)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AIInsightResponse getInsight(
            Long merchantId,
            Long insightId
    ) {
        AIInsight insight = insightRepository
                .findByMerchantIdAndId(merchantId, insightId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("AI insight not found"));

        return AIInsightResponse.from(insight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIInsightResponse> getInsights(
            Long merchantId
    ) {
        return insightRepository
                .findByMerchantIdOrderByValidFromDesc(merchantId)
                .stream()
                .map(AIInsightResponse::from)
                .toList();
    }
}