package com.paypilot.backend.domain.payment;

import com.paypilot.backend.domain.payment.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse getPayment(Long merchantId, Long paymentId);
}