package com.paypilot.backend.domain.order;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindOrderByMerchantIdAndRazorpayOrderId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Order order = orderRepository.save(
                new Order(
                        merchant,
                        "order_test_123",
                        new BigDecimal("1500.00"),
                        "INR",
                        "receipt_123"
                )
        );

        var result = orderRepository.findByMerchantIdAndRazorpayOrderId(
                merchant.getId(),
                "order_test_123"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("1500.00");
        assertThat(result.get().getStatus())
                .isEqualTo(OrderStatus.CREATED);
        assertThat(result.get().getAmountPaid())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getAmountDue())
                .isEqualByComparingTo("1500.00");
        assertThat(result.get().getAttempts()).isZero();
    }

    @Test
    void shouldNotFindOrderWhenMerchantIdDoesNotMatch() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "acc_order_first",
                        "First Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "acc_order_second",
                        "Second Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        orderRepository.save(
                new Order(
                        firstMerchant,
                        "order_shared_test_123",
                        new BigDecimal("1500.00"),
                        "INR",
                        "receipt_123"
                )
        );

        var result = orderRepository.findByMerchantIdAndRazorpayOrderId(
                secondMerchant.getId(),
                "order_shared_test_123"
        );

        assertThat(result).isEmpty();
    }
}