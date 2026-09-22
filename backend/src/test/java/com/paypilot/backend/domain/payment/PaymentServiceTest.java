package com.paypilot.backend.domain.payment;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.payment.dto.PaymentResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldGetPaymentForMerchant() {
        Merchant merchant = mock(Merchant.class);

        when(merchant.getId()).thenReturn(10L);

        Payment payment = new Payment(
                merchant,
                "pay_service_123",
                "order_service_123",
                new BigDecimal("1500.00"),
                "INR",
                "card"
        );

        when(paymentRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPayment(10L, 1L);

        assertThat(response.razorpayPaymentId())
                .isEqualTo("pay_service_123");
        assertThat(response.amount())
                .isEqualByComparingTo("1500.00");
        assertThat(response.currency())
                .isEqualTo("INR");
        assertThat(response.status())
                .isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.getPayment(10L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found: 1");
    }

    @Test
    void shouldNotReturnPaymentFromAnotherMerchant() {
        when(paymentRepository.findByMerchantIdAndId(20L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.getPayment(20L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found: 1");
    }
}