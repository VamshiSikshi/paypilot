package com.paypilot.backend.domain.order;

import com.paypilot.backend.domain.order.dto.OrderResponse;

public interface OrderService {

    OrderResponse getOrder(Long merchantId, Long orderId);
}