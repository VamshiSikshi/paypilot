package com.paypilot.backend.controller;

import com.paypilot.backend.domain.order.OrderService;
import com.paypilot.backend.domain.order.OrderStatus;
import com.paypilot.backend.domain.order.dto.OrderResponse;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.exception.ResourceNotFoundException;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private AuthenticatedUserContext authenticatedUserContext;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnOrderForAuthenticatedMerchant() throws Exception {
        Long merchantId = 10L;
        Long orderId = 25L;
        Instant createdAt = Instant.parse("2026-10-11T04:00:00Z");

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(orderService.getOrder(merchantId, orderId))
                .willReturn(new OrderResponse(
                        orderId,
                        merchantId,
                        "order_test123",
                        new BigDecimal("150.00"),
                        "INR",
                        OrderStatus.CREATED,
                        "receipt_test123",
                        createdAt,
                        createdAt,
                        null,
                        BigDecimal.ZERO,
                        new BigDecimal("150.00"),
                        0,
                        null
                ));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.merchantId").value(merchantId))
                .andExpect(jsonPath("$.razorpayOrderId")
                        .value("order_test123"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        then(orderService).should().getOrder(merchantId, orderId);
    }

    @Test
    void shouldReturn404WhenOrderIsNotFound() throws Exception {
        Long merchantId = 10L;
        Long orderId = 999L;

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(orderService.getOrder(merchantId, orderId))
                .willThrow(new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Order not found: " + orderId));

        then(orderService).should().getOrder(merchantId, orderId);
    }
}