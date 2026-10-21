package com.paypilot.backend.domain.razorpay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RazorpayConnectionRepository
        extends JpaRepository<RazorpayConnection, Long> {

    Optional<RazorpayConnection> findByMerchantId(Long merchantId);

    Optional<RazorpayConnection> findByExternalAccountId(
            String externalAccountId
    );
}