package com.paypilot.backend.controller;

import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.dto.auth.LoginRequest;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.user.UserStatus;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldLoginWithEmailAndPassword() throws Exception {
        given(authService.login(
                org.mockito.ArgumentMatchers.any(LoginRequest.class)
        )).willReturn(new AuthResponse(
                "test-access-token",
                "Bearer",
                3600L,
                new CurrentUserResponse(
                        5L, 10L, "owner@example.com",
                        "Merchant Owner", UserRole.OWNER, UserStatus.ACTIVE
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
                .andExpect(jsonPath("$.accessToken")
                        .value("test-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email")
                        .value("owner@example.com"));

        then(authService).should().login(
                org.mockito.ArgumentMatchers.any(LoginRequest.class)
        );
    }

    @Test
    void shouldLoginWithGoogle() throws Exception {
        given(authService.loginWithGoogle(
                org.mockito.ArgumentMatchers.any(GoogleLoginRequest.class)
        )).willReturn(new AuthResponse(
                "google-access-token",
                "Bearer",
                3600L,
                new CurrentUserResponse(
                        5L, 10L, "owner@example.com",
                        "Merchant Owner", UserRole.OWNER, UserStatus.ACTIVE
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
                .andExpect(jsonPath("$.accessToken")
                        .value("google-access-token"))
                .andExpect(jsonPath("$.user.role").value("OWNER"));

        then(authService).should().loginWithGoogle(
                org.mockito.ArgumentMatchers.any(GoogleLoginRequest.class)
        );
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        given(authService.getCurrentUser())
                .willReturn(new CurrentUserResponse(
                        5L, 10L, "owner@example.com",
                        "Merchant Owner", UserRole.OWNER, UserStatus.ACTIVE
                ));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.merchantId").value(10))
                .andExpect(jsonPath("$.email").value("owner@example.com"));

        then(authService).should().getCurrentUser();
    }
}