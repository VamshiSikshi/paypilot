
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private AuthenticatedUserContext authenticatedUserContext;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnAuditLogsForAuthenticatedMerchant() throws Exception {
        Long merchantId = 10L;
        Instant createdAt = Instant.parse("2026-10-11T04:00:00Z");

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(auditLogService.getAuditLogs(merchantId))
                .willReturn(List.of(
                        new AuditLogResponse(
                                1L,
                                merchantId,
                                5L,
                                "PAYMENT_CAPTURED",
                                "PAYMENT",
                                25L,
                                "Payment captured",
                                null,
                                null,
                                null,
                                createdAt
                        )
                ));

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].merchantId").value(10))
                .andExpect(jsonPath("$[0].eventType")
                        .value("PAYMENT_CAPTURED"))
                .andExpect(jsonPath("$[0].resourceType")
                        .value("PAYMENT"));

        then(auditLogService).should().getAuditLogs(merchantId);
    }

    @Test
    void shouldReturnEmptyListWhenNoAuditLogsExist() throws Exception {
        Long merchantId = 10L;

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.VIEWER
                ));

        given(auditLogService.getAuditLogs(merchantId))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        then(auditLogService).should().getAuditLogs(merchantId);
    }
}
