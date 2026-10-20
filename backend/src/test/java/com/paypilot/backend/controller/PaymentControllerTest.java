
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.payment.PaymentService;
import com.paypilot.backend.domain.payment.dto.PaymentResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.paypilot.backend.exception.ResourceNotFoundException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private AuthenticatedUserContext authenticatedUserContext;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnPaymentForAuthenticatedMerchant() throws Exception {
        Long merchantId = 10L;
        Long paymentId = 25L;

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(paymentService.getPayment(merchantId, paymentId))
                .willReturn(new PaymentResponse(
                        paymentId,
                        merchantId,
                        "pay_test123",
                        "order_test123",
                        new BigDecimal("150.00"),
                        "INR",
                        null,
                        "card",
                        Instant.parse("2026-10-11T04:00:00Z"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Instant.parse("2026-10-11T04:00:00Z"),
                        Instant.parse("2026-10-11T04:00:00Z"),
                        null,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        true,
                        false,
                        "Test payment",
                        null
                ));

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.merchantId").value(merchantId))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("INR"));

        then(paymentService).should()
                .getPayment(merchantId, paymentId);
    }

    @Test
    void shouldReturn404WhenPaymentIsNotFound() throws Exception {
        Long merchantId = 10L;
        Long paymentId = 999L;

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(paymentService.getPayment(merchantId, paymentId))
                .willThrow(new ResourceNotFoundException(
                        "Payment not found: " + paymentId
                ));

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Payment not found: " + paymentId));
    }
}
