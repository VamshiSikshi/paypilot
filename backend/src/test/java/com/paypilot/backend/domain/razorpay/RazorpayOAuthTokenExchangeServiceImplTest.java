package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import com.paypilot.backend.domain.razorpay.dto.RazorpayOAuthTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RazorpayOAuthTokenExchangeServiceImplTest {

    private static final String TOKEN_URI =
            "https://auth.razorpay.com/token";

    private MockRestServiceServer server;
    private RazorpayOAuthTokenExchangeServiceImpl service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "https://app.example.com/api/v1/razorpay/callback",
                "payments:read",
                "https://auth.razorpay.com/authorize",
                TOKEN_URI,
                "test"
        );

        service = new RazorpayOAuthTokenExchangeServiceImpl(
                builder,
                properties
        );
    }

    @Test
    void shouldExchangeCodeAndMapTokenResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.client_id").value("test-client-id"))
                .andExpect(jsonPath("$.client_secret").value("test-client-secret"))
                .andExpect(jsonPath("$.grant_type").value("authorization_code"))
                .andExpect(jsonPath("$.redirect_uri").value(
                        "https://app.example.com/api/v1/razorpay/callback"
                ))
                .andExpect(jsonPath("$.code").value("test-auth-code"))
                .andExpect(jsonPath("$.mode").value("test"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "refresh_token": "test-refresh-token",
                          "expires_in": 3600,
                          "razorpay_account_id": "acc_test_123",
                          "scope": "payments:read",
                          "token_type": "Bearer"
                        }
                        """, MediaType.APPLICATION_JSON));

        RazorpayOAuthTokenResponse response =
                service.exchangeAuthorizationCode("test-auth-code");

        assertEquals("test-access-token", response.accessToken());
        assertEquals("test-refresh-token", response.refreshToken());
        assertEquals(3600L, response.expiresIn());
        assertEquals("acc_test_123", response.razorpayAccountId());
        assertEquals("payments:read", response.scope());
        assertEquals("Bearer", response.tokenType());

        server.verify();
    }

    @Test
    void shouldRejectBlankAuthorizationCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.exchangeAuthorizationCode(" ")
        );

        server.verify();
    }

    @Test
    void shouldRejectHttpTokenEndpoint() {
        RazorpayOAuthProperties insecureProperties =
                new RazorpayOAuthProperties(
                        "test-client-id",
                        "test-client-secret",
                        "https://app.example.com/callback",
                        "payments:read",
                        "https://auth.razorpay.com/authorize",
                        "http://auth.razorpay.com/token",
                        "test"
                );

        RazorpayOAuthTokenExchangeServiceImpl insecureService =
                new RazorpayOAuthTokenExchangeServiceImpl(
                        RestClient.builder(),
                        insecureProperties
                );

        assertThrows(
                IllegalStateException.class,
                () -> insecureService.exchangeAuthorizationCode(
                        "test-auth-code"
                )
        );
    }

    @Test
    void shouldRejectMissingAccessTokenInResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("""
                        {
                          "refresh_token": "test-refresh-token",
                          "expires_in": 3600,
                          "razorpay_account_id": "acc_test_123"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> service.exchangeAuthorizationCode("test-auth-code")
        );

        server.verify();
    }

    @Test
    void shouldRejectMissingAccountIdInResponse() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "refresh_token": "test-refresh-token",
                          "expires_in": 3600
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> service.exchangeAuthorizationCode("test-auth-code")
        );

        server.verify();
    }

    @Test
    void shouldRejectNonPositiveExpiry() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "razorpay_account_id": "acc_test_123",
                          "expires_in": 0
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThrows(
                IllegalStateException.class,
                () -> service.exchangeAuthorizationCode("test-auth-code")
        );

        server.verify();
    }

    @Test
    void shouldRejectUnsuccessfulHttpResponseWithoutLeakingSecrets() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withStatus(
                        org.springframework.http.HttpStatus.UNAUTHORIZED
                ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.exchangeAuthorizationCode("test-auth-code")
        );

        assertFalse(exception.getMessage().contains("test-client-secret"));
        assertFalse(exception.getMessage().contains("test-auth-code"));

        server.verify();
    }
}
