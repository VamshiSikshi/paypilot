package com.paypilot.backend.domain.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SettlementReconciliationRepository
        extends JpaRepository<SettlementReconciliation, Long> {

    Optional<SettlementReconciliation> findByMerchantIdAndRazorpayEntityId(
            Long merchantId,
            String razorpayEntityId
    );

    Optional<SettlementReconciliation> findByMerchantIdAndSettlementIdAndRazorpayEntityIdAndTransactionType(
            Long merchantId,
            Long settlementId,
            String razorpayEntityId,
            ReconciliationTransactionType transactionType
    );

    List<SettlementReconciliation> findAllByMerchantIdAndSettlementId(
            Long merchantId,
            Long settlementId
    );

    Optional<SettlementReconciliation> findByMerchantIdAndId(
            Long merchantId,
            Long reconciliationId
    );
}