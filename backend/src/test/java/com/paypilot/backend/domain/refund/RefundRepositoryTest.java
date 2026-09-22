package com.paypilot.backend.domain.refund;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.payment.Payment;
import com.paypilot.backend.domain.payment.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class RefundRepositoryTest {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindRefundByMerchantIdAndRazorpayRefundId() {
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

        Refund refund = refundRepository.save(
                new Refund(
                        merchant,
                        payment,
                        "rfnd_test_123",
                        "pay_test_123",
                        new BigDecimal("500.00"),
                        "INR"
                )
        );

        var result = refundRepository.findByMerchantIdAndRazorpayRefundId(
                merchant.getId(),
                "rfnd_test_123"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(refund.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getPayment().getId())
                .isEqualTo(payment.getId());
        assertThat(result.get().getRazorpayRefundId())
                .isEqualTo("rfnd_test_123");
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("500.00");
        assertThat(result.get().getCurrency())
                .isEqualTo("INR");
        assertThat(result.get().getStatus())
                .isEqualTo(RefundStatus.CREATED);
    }

    @Test
    void shouldFindRefundByMerchantIdAndId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_refund_lookup",
                        "Refund Lookup Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Payment payment = paymentRepository.save(
                new Payment(
                        merchant,
                        "pay_refund_lookup",
                        "order_refund_lookup",
                        new BigDecimal("1500.00"),
                        "INR",
                        "card"
                )
        );

        Refund refund = refundRepository.save(
                new Refund(
                        merchant,
                        payment,
                        "rfnd_lookup_123",
                        "pay_refund_lookup",
                        new BigDecimal("500.00"),
                        "INR"
                )
        );

        var result = refundRepository.findByMerchantIdAndId(
                merchant.getId(),
                refund.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(refund.getId());
    }

    @Test
    void shouldNotFindRefundWhenMerchantIdDoesNotMatch() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "acc_refund_first",
                        "First Refund Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "acc_refund_second",
                        "Second Refund Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Payment payment = paymentRepository.save(
                new Payment(
                        firstMerchant,
                        "pay_refund_shared",
                        "order_refund_shared",
                        new BigDecimal("1500.00"),
                        "INR",
                        "card"
                )
        );

        Refund refund = refundRepository.save(
                new Refund(
                        firstMerchant,
                        payment,
                        "rfnd_shared_123",
                        "pay_refund_shared",
                        new BigDecimal("500.00"),
                        "INR"
                )
        );

        var result = refundRepository.findByMerchantIdAndId(
                secondMerchant.getId(),
                refund.getId()
        );

        assertThat(result).isEmpty();
    }
}