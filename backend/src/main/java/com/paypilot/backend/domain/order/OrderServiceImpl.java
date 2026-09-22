package com.paypilot.backend.domain.order;

import com.paypilot.backend.domain.order.dto.OrderResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResponse getOrder(Long merchantId, Long orderId) {
        Order order = orderRepository.findByMerchantIdAndId(merchantId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));

        return OrderResponse.from(order);
    }
}