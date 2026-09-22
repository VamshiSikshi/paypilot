package com.paypilot.backend.domain.payment;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindPaymentByMerchantIdAndRazorpayPaymentId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Payment payment = paymentRepository.save(
                new Payment(
                        merchant,
                        "pay_test_123",
                        "order_test_123",
                        new BigDecimal("1500.00"),
                        "INR",
                        "card"
                )
        );

        var result = paymentRepository.findByMerchantIdAndRazorpayPaymentId(
                merchant.getId(),
                "pay_test_123"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(payment.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getRazorpayPaymentId())
                .isEqualTo("pay_test_123");
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("1500.00");
        assertThat(result.get().getStatus())
                .isEqualTo(PaymentStatus.CREATED);
        assertThat(result.get().getRefundStatus())
                .isEqualTo(RefundStatus.NOT_REFUNDED);
        assertThat(result.get().getAmountRefunded())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getCaptured())
                .isFalse();
    }

    @Test
    void shouldFindPaymentByMerchantIdAndId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_payment_lookup",
                        "Payment Lookup Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Payment payment = paymentRepository.save(
                new Payment(
                        merchant,
                        "pay_lookup_123",
                        "order_lookup_123",
                        new BigDecimal("1500.00"),
                        "INR",
                        "card"
                )
        );

        var result = paymentRepository.findByMerchantIdAndId(
                merchant.getId(),
                payment.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(payment.getId());
    }
}