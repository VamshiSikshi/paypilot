package com.paypilot.backend.domain.payment;

import com.paypilot.backend.domain.payment.dto.PaymentResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponse getPayment(Long merchantId, Long paymentId) {
        Payment payment = paymentRepository
                .findByMerchantIdAndId(merchantId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId
                ));

        return PaymentResponse.from(payment);
    }
}