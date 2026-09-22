package com.paypilot.backend.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantIdAndRazorpayPaymentId(
            Long merchantId,
            String razorpayPaymentId
    );

    Optional<Payment> findByMerchantIdAndId(
            Long merchantId,
            Long paymentId
    );
}