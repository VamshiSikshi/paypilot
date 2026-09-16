package com.paypilot.backend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIToolExecutionRepository
        extends JpaRepository<AIToolExecution, Long> {

    Optional<AIToolExecution> findByMerchantIdAndToolCallId(
            Long merchantId,
            String toolCallId
    );
}