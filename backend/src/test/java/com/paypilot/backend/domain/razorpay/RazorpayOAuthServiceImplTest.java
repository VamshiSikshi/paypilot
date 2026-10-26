package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayOAuthServiceImplTest {

    @Mock
    private RazorpayOAuthStateService stateService;

    private RazorpayOAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "https://app.example.com/api/v1/razorpay/callback",
                "read_write",
                "https://auth.razorpay.com/authorize",
            "https://auth.razorpay.com/token",
            "test"
        );

        service = new RazorpayOAuthServiceImpl(properties, stateService);
    }

    @Test
    void shouldBuildAuthorizationUrlWithRequiredParameters() {
        Long merchantId = 10L;
        String rawState = "test-state_123";

        when(stateService.createState(merchantId)).thenReturn(rawState);

        String authorizationUrl = service.createAuthorizationUrl(merchantId);

        Map<String, String> queryParameters =
                UriComponentsBuilder.fromUriString(authorizationUrl)
                        .build()
                        .getQueryParams()
                        .toSingleValueMap();

        assertEquals(
                "https://auth.razorpay.com/authorize",
                UriComponentsBuilder.fromUriString(authorizationUrl)
                        .build()
                        .getPath() == null
                        ? authorizationUrl
                        : "https://auth.razorpay.com" +
                                UriComponentsBuilder.fromUriString(authorizationUrl)
                                        .build().getPath()
        );
        assertEquals("test-client-id", queryParameters.get("client_id"));
        assertEquals("code", queryParameters.get("response_type"));
        assertEquals(
                "https://app.example.com/api/v1/razorpay/callback",
                queryParameters.get("redirect_uri")
        );
        assertEquals("read_write", queryParameters.get("scope"));
        assertEquals(rawState, queryParameters.get("state"));

        verify(stateService).createState(merchantId);
    }

    @Test
    void shouldRejectMissingClientIdBeforeCreatingState() {
        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "",
                "test-client-secret",
                "https://app.example.com/api/v1/razorpay/callback",
                "read_write",
                "https://auth.razorpay.com/authorize",
            "https://auth.razorpay.com/token",
            "test"
        );

        service = new RazorpayOAuthServiceImpl(properties, stateService);

        assertThrows(
                IllegalStateException.class,
                () -> service.createAuthorizationUrl(10L)
        );

        verifyNoInteractions(stateService);
    }

    @Test
    void shouldRejectInsecureRedirectUri() {
        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "http://app.example.com/api/v1/razorpay/callback",
                "read_write",
                "https://auth.razorpay.com/authorize",
            "https://auth.razorpay.com/token",
            "test"
        );

        service = new RazorpayOAuthServiceImpl(properties, stateService);

        assertThrows(
                IllegalStateException.class,
                () -> service.createAuthorizationUrl(10L)
        );

        verifyNoInteractions(stateService);
    }

    @Test
    void shouldRejectInsecureAuthorizationUri() {
        RazorpayOAuthProperties properties = new RazorpayOAuthProperties(
                "test-client-id",
                "test-client-secret",
                "https://app.example.com/api/v1/razorpay/callback",
                "read_write",
                "http://auth.razorpay.com/authorize",
            "https://auth.razorpay.com/token",
            "test"
        );

        service = new RazorpayOAuthServiceImpl(properties, stateService);

        assertThrows(
                IllegalStateException.class,
                () -> service.createAuthorizationUrl(10L)
        );

        verifyNoInteractions(stateService);
    }
}
