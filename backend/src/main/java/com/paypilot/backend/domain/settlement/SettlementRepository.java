package com.paypilot.backend.domain.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByMerchantIdAndRazorpaySettlementId(
            Long merchantId,
            String razorpaySettlementId
    );
}