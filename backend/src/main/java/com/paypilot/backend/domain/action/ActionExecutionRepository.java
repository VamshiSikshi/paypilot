package com.paypilot.backend.domain.action;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionExecutionRepository
        extends JpaRepository<ActionExecution, Long> {

    Optional<ActionExecution> findByMerchantIdAndIdempotencyKey(
            Long merchantId,
            String idempotencyKey
    );
}