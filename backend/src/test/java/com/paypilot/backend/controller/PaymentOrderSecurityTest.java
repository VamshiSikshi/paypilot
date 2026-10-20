package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.order.OrderService;
import com.paypilot.backend.domain.payment.PaymentService;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.refund.RefundService;
import com.paypilot.backend.controller.RefundController;
import com.paypilot.backend.domain.sync.SyncService;
import com.paypilot.backend.domain.settlement.SettlementService;
import com.paypilot.backend.domain.reconciliation.ReconciliationService;

@WebMvcTest(controllers = {
        PaymentController.class,
        OrderController.class,
        AuditController.class,
        RefundController.class,
        SyncController.class,
        SettlementController.class,
        ReconciliationController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthenticatedUserContext.class,
        PaymentOrderSecurityTest.FilterTestConfiguration.class
})
class PaymentOrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private RefundService refundService;

    @MockitoBean
    private SyncService syncService;

    @MockitoBean
    private SettlementService settlementService;

    @MockitoBean
    private ReconciliationService reconciliationService;

    @Test
    void shouldRejectPaymentRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/payments/25"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectOrderRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/orders/25"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAuditLogsRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadPayments(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/payments/25")
                    .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(paymentService).should()
                .getPayment(10L, 25L);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadOrders(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/orders/25")
                .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(orderService).should()
                .getOrder(10L, 25L);
    }

    private void authenticateAs(UserRole role) {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn(role.name());

        given(jwtService.parseToken(bearerToken(role)))
                .willReturn(claims);
    }

    private String bearerToken(UserRole role) {
        return role.name().toLowerCase() + "-test-token";
    }

    @TestConfiguration
    static class FilterTestConfiguration {

        @Bean
        FilterRegistrationBean<JwtAuthenticationFilter>
        jwtFilterServletRegistration(JwtAuthenticationFilter filter) {

            FilterRegistrationBean<JwtAuthenticationFilter> registration =
                    new FilterRegistrationBean<>(filter);

            registration.setEnabled(false);
            return registration;
        }
    }

    @Test
    void shouldRejectRefundRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/refunds/25"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadRefunds(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/refunds/25")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(refundService).should()
                .getRefund(10L, 25L);
    }

    @Test
    void shouldRejectSyncJobsRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/sync/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadSyncJobs(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/sync/jobs")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(syncService).should()
                .getSyncJobs(10L);
    }

    @Test
    void shouldRejectSettlementRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/settlements/25"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadSettlements(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/settlements/25")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(settlementService).should()
                .getSettlement(10L, 25L);
    }


    @Test
    void shouldRejectReconciliationRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/25"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadReconciliation(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/reconciliations/25")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(reconciliationService).should()
                .getReconciliation(10L, 25L);
    }

    @Test
    void shouldRejectSettlementReconciliationsRequestWithoutJwt()
            throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations")
                        .param("settlementId", "25"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadReconciliationsBySettlement(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/reconciliations")
                        .param("settlementId", "25")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(reconciliationService).should()
                .getReconciliationsBySettlement(10L, 25L);
    }

    @Test
    void shouldRejectRazorpayEntityReconciliationRequestWithoutJwt()
            throws Exception {
        mockMvc.perform(get("/api/v1/reconciliations/by-razorpay-entity")
                        .param("razorpayEntityId", "pay_test_123"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadReconciliationByRazorpayEntity(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/reconciliations/by-razorpay-entity")
                        .param("razorpayEntityId", "pay_test_123")
                        .header("Authorization",
                                "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(reconciliationService).should()
                .getReconciliationByRazorpayEntity(10L, "pay_test_123");
    }


    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowEveryRoleToReadAuditLogs(UserRole role)
            throws Exception {

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(auditLogService).should()
                .getAuditLogs(10L);
    }
}