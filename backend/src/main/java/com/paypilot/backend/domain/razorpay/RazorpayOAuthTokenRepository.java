package com.paypilot.backend.domain.razorpay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RazorpayOAuthTokenRepository
        extends JpaRepository<RazorpayOAuthToken, Long> {

    Optional<RazorpayOAuthToken> findByMerchantId(Long merchantId);
}