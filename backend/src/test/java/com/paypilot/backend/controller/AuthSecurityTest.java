
package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.dto.auth.LoginRequest;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.user.UserStatus;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.security.JwtService;
import com.paypilot.backend.service.AuthService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthSecurityTest.FilterTestConfiguration.class
})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldAllowLoginWithoutJwt() throws Exception {
        given(authService.login(any(LoginRequest.class)))
                .willReturn(new AuthResponse(
                        "test-token",
                        "Bearer",
                        3600L,
                        new CurrentUserResponse(
                                5L, 10L, "owner@example.com",
                                "Owner", UserRole.OWNER, UserStatus.ACTIVE
                        )
                ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantAccountId": "merchant_test",
                                  "email": "owner@example.com",
                                  "password": "test-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-token"));
    }

    @Test
    void shouldAllowGoogleLoginWithoutJwt() throws Exception {
        given(authService.loginWithGoogle(any(GoogleLoginRequest.class)))
                .willReturn(new AuthResponse(
                        "google-token",
                        "Bearer",
                        3600L,
                        new CurrentUserResponse(
                                5L, 10L, "owner@example.com",
                                "Owner", UserRole.OWNER, UserStatus.ACTIVE
                        )
                ));

        mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantAccountId": "merchant_test",
                                  "idToken": "test-google-id-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("google-token"));
    }

    @Test
    void shouldRejectCurrentUserRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCurrentUserRequestWithInvalidJwt() throws Exception {
        given(jwtService.parseToken("invalid-token"))
                .willThrow(new IllegalArgumentException("Invalid token"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowCurrentUserRequestWithValidJwt() throws Exception {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn("OWNER");
        given(jwtService.parseToken("valid-token")).willReturn(claims);

        given(authService.getCurrentUser())
                .willReturn(new CurrentUserResponse(
                        5L, 10L, "owner@example.com",
                        "Owner", UserRole.OWNER, UserStatus.ACTIVE
                ));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.merchantId").value(10))
                .andExpect(jsonPath("$.email").value("owner@example.com"));
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
