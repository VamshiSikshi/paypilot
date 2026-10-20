package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

@WebMvcTest(AuditController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthenticatedUserContext.class,
        AuditSecurityTest.FilterTestConfiguration.class
})
class AuditSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtService jwtService;

    private String bearerToken(UserRole role) {
        return role.name().toLowerCase() + "-audit-test-token";
    }

    private void authenticateAs(UserRole role) {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn(role.name());

        given(jwtService.parseToken(bearerToken(role))).willReturn(claims);
    }

    @Test
    void shouldRejectAuditLogReadWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized());

        then(auditLogService).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAllAuthenticatedRolesToReadAuditLogs(UserRole role)
            throws Exception {
        authenticateAs(role);

        mockMvc.perform(get("/api/v1/audit-logs")
                        .header(
                                "Authorization",
                                "Bearer " + bearerToken(role)
                        ))
                .andExpect(status().isOk());

        then(auditLogService).should().getAuditLogs(10L);
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
}