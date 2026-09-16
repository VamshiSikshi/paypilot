package com.paypilot.backend.domain.refund;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByMerchantIdAndRazorpayRefundId(
            Long merchantId,
            String razorpayRefundId
    );
}