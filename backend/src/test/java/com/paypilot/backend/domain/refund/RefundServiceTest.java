package com.paypilot.backend.domain.refund;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.payment.Payment;
import com.paypilot.backend.domain.refund.dto.RefundResponse;
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
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private RefundServiceImpl refundService;

    @Test
    void shouldGetRefundForMerchant() {
        Merchant merchant = mock(Merchant.class);
        Payment payment = mock(Payment.class);

        when(merchant.getId()).thenReturn(10L);
        when(payment.getId()).thenReturn(100L);

        Refund refund = new Refund(
                merchant,
                payment,
                "rfnd_service_123",
                "pay_service_123",
                new BigDecimal("500.00"),
                "INR"
        );

        when(refundRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(refund));

        RefundResponse response = refundService.getRefund(10L, 1L);

        assertThat(response.razorpayRefundId())
                .isEqualTo("rfnd_service_123");
        assertThat(response.razorpayPaymentId())
                .isEqualTo("pay_service_123");
        assertThat(response.amount())
                .isEqualByComparingTo("500.00");
        assertThat(response.currency())
                .isEqualTo("INR");
        assertThat(response.status())
                .isEqualTo(RefundStatus.CREATED);
        assertThat(response.paymentId())
                .isEqualTo(100L);
    }

    @Test
    void shouldThrowWhenRefundDoesNotExist() {
        when(refundRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                refundService.getRefund(10L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Refund not found: 1");
    }

    @Test
    void shouldNotReturnRefundFromAnotherMerchant() {
        when(refundRepository.findByMerchantIdAndId(20L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                refundService.getRefund(20L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Refund not found: 1");
    }
}