package com.paypilot.backend.controller;

import com.paypilot.backend.domain.order.OrderService;
import com.paypilot.backend.domain.order.dto.OrderResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public OrderController(
            OrderService orderService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.orderService = orderService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return orderService.getOrder(
                user.merchantId(),
                orderId
        );
    }
}