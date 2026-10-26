package com.paypilot.backend.controller;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthConnectionService;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthStateService;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthTokenExchangeService;
import com.paypilot.backend.domain.razorpay.dto.RazorpayOAuthTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayOAuthCallbackControllerTest {

    @Mock
    private RazorpayOAuthStateService stateService;

    @Mock
    private RazorpayOAuthTokenExchangeService tokenExchangeService;

    @Mock
    private RazorpayOAuthConnectionService connectionService;

    private RazorpayOAuthCallbackController controller;

    @BeforeEach
    void setUp() {
        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "https://app.example.com/api/v1/razorpay/callback",
                "payments:read",
                "https://auth.razorpay.com/authorize",
                "https://auth.razorpay.com/token",
                "test"
        );

        controller = new RazorpayOAuthCallbackController(
                stateService,
                tokenExchangeService,
                connectionService,
                properties
        );
    }

    @Test
    void shouldExchangeCodeAndConnectMerchantWithoutReturningTokens() {
        when(stateService.consumeStateAndGetMerchantId("valid-state"))
                .thenReturn(Optional.of(10L));

        when(tokenExchangeService.exchangeAuthorizationCode("test-code"))
                .thenReturn(new RazorpayOAuthTokenResponse(
                        "test-access-token",
                        "test-refresh-token",
                        3600L,
                        "acc_test_123",
                        "payments:read",
                        "Bearer"
                ));

        var response = controller.handleCallback(
                "test-code",
                "valid-state",
                null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Razorpay account connected successfully.",
                response.getBody()
        );
        assertFalse(response.getBody().contains("test-access-token"));
        assertFalse(response.getBody().contains("test-refresh-token"));

        verify(stateService).consumeStateAndGetMerchantId("valid-state");
        verify(tokenExchangeService).exchangeAuthorizationCode("test-code");
        verify(connectionService).completeAuthorization(
                eq(10L),
                eq("acc_test_123"),
                eq("test-access-token"),
                eq("test-refresh-token"),
                any(),
                eq("payments:read")
        );
    }

    @Test
    void shouldRejectMissingState() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleCallback("test-code", " ", null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(stateService, tokenExchangeService,
                connectionService);
    }

    @Test
    void shouldRejectInvalidOrReusedState() {
        when(stateService.consumeStateAndGetMerchantId("invalid-state"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleCallback(
                        "test-code", "invalid-state", null
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(tokenExchangeService, connectionService);
    }

    @Test
    void shouldRejectProviderAuthorizationError() {
        when(stateService.consumeStateAndGetMerchantId("valid-state"))
                .thenReturn(Optional.of(10L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleCallback(
                        null, "valid-state", "access_denied"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(tokenExchangeService, connectionService);
    }

    @Test
    void shouldRejectMissingAuthorizationCode() {
        when(stateService.consumeStateAndGetMerchantId("valid-state"))
                .thenReturn(Optional.of(10L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleCallback(
                        " ", "valid-state", null
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(tokenExchangeService, connectionService);
    }

    @Test
    void shouldReturnBadGatewayWhenTokenExchangeFails() {
        when(stateService.consumeStateAndGetMerchantId("valid-state"))
                .thenReturn(Optional.of(10L));

        when(tokenExchangeService.exchangeAuthorizationCode("test-code"))
                .thenThrow(new IllegalStateException("Token exchange failed"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleCallback(
                        "test-code", "valid-state", null
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertFalse(exception.getReason().contains("test-code"));
        verifyNoInteractions(connectionService);
    }
}
