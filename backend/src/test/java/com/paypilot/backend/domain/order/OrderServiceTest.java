package com.paypilot.backend.domain.order;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.order.dto.OrderResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldGetOrderForMerchant() {
        Merchant merchant = mock(Merchant.class);

        when(merchant.getId()).thenReturn(10L);

        Order order = new Order(
                merchant,
                "order_service_123",
                new BigDecimal("1500.00"),
                "INR",
                "receipt_123"
        );

        when(orderRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(10L, 1L);

        assertThat(response.razorpayOrderId())
                .isEqualTo("order_service_123");
        assertThat(response.amount())
                .isEqualByComparingTo("1500.00");
        assertThat(response.currency())
                .isEqualTo("INR");
        assertThat(response.status())
                .isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.getOrder(10L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found: 1");
    }

    @Test
    void shouldNotReturnOrderFromAnotherMerchant() {
        when(orderRepository.findByMerchantIdAndId(20L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.getOrder(20L, 1L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found: 1");
    }
}