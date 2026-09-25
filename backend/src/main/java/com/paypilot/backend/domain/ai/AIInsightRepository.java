package com.paypilot.backend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {

    Optional<AIInsight> findByMerchantIdAndId(
            Long merchantId,
            Long id
    );

    List<AIInsight> findByMerchantIdOrderByValidFromDesc(
            Long merchantId
    );
}