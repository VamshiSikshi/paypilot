package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.merchant.MerchantService;
import com.paypilot.backend.domain.merchant.MerchantStatus;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthenticatedUserContext.class,
        MerchantSecurityTest.FilterTestConfiguration.class
})
class MerchantSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JwtService jwtService;

    private String bearerToken(UserRole role) {
        return role.name().toLowerCase() + "-merchant-test-token";
    }

    private void authenticateAs(UserRole role) {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn(role.name());

        given(jwtService.parseToken(bearerToken(role))).willReturn(claims);
    }

    @Test
    void shouldRejectMerchantReadWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/merchant"))
                .andExpect(status().isUnauthorized());

        then(merchantService).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAllAuthenticatedRolesToReadMerchant(UserRole role)
            throws Exception {
        authenticateAs(role);

        mockMvc.perform(get("/api/v1/merchant")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(merchantService).should().getMerchant(10L);
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"FINANCE", "VIEWER"})
    void shouldDenyNonOwnerRolesFromUpdatingMerchantStatus(UserRole role)
            throws Exception {
        authenticateAs(role);

        mockMvc.perform(patch("/api/v1/merchant/status")
                        .header("Authorization", "Bearer " + bearerToken(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "SUSPENDED"}
                                """))
                .andExpect(status().isForbidden());

        then(merchantService).shouldHaveNoInteractions();
    }

    @Test
    void shouldRejectMerchantStatusUpdateWithoutJwt() throws Exception {
        mockMvc.perform(patch("/api/v1/merchant/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "SUSPENDED"}
                                """))
                .andExpect(status().isUnauthorized());

        then(merchantService).shouldHaveNoInteractions();
    }

    @Test
    void shouldAllowOwnerToUpdateMerchantStatus() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(patch("/api/v1/merchant/status")
                        .header(
                                "Authorization",
                                "Bearer " + bearerToken(UserRole.OWNER)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "SUSPENDED"}
                                """))
                .andExpect(status().isOk());

        then(merchantService).should().updateStatus(
                10L,
                MerchantStatus.SUSPENDED
        );
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