package com.paypilot.backend.domain.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementReconciliationRepository
        extends JpaRepository<SettlementReconciliation, Long> {

    Optional<SettlementReconciliation> findByMerchantIdAndRazorpayEntityId(
            Long merchantId,
            String razorpayEntityId
    );
}