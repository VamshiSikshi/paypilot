package com.paypilot.backend.domain.merchant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByRazorpayAccountId(String razorpayAccountId);
}