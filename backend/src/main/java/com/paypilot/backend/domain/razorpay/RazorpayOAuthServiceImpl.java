package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@Service
public class RazorpayOAuthServiceImpl implements RazorpayOAuthService {

    private final RazorpayOAuthProperties properties;
    private final RazorpayOAuthStateService stateService;

    public RazorpayOAuthServiceImpl(
            RazorpayOAuthProperties properties,
            RazorpayOAuthStateService stateService
    ) {
        this.properties = Objects.requireNonNull(properties);
        this.stateService = Objects.requireNonNull(stateService);
    }

    @Override
    public String createAuthorizationUrl(Long merchantId) {
        requireConfigured(properties.clientId(), "Razorpay OAuth client ID");
        requireConfigured(properties.redirectUri(), "Razorpay OAuth redirect URI");
        requireConfigured(properties.scope(), "Razorpay OAuth scope");
        requireConfigured(
                properties.authorizationUri(),
                "Razorpay OAuth authorization URI"
        );

        URI redirectUri = URI.create(properties.redirectUri());
        URI authorizationUri = URI.create(properties.authorizationUri());

        if (!"https".equalsIgnoreCase(redirectUri.getScheme())) {
            throw new IllegalStateException(
                    "Razorpay OAuth redirect URI must use HTTPS"
            );
        }

        if (!"https".equalsIgnoreCase(authorizationUri.getScheme())) {
            throw new IllegalStateException(
                    "Razorpay OAuth authorization URI must use HTTPS"
            );
        }

        String state = stateService.createState(merchantId);

        return UriComponentsBuilder
                .fromUri(authorizationUri)
                .queryParam("client_id", properties.clientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", properties.scope())
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    private void requireConfigured(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(
                    propertyName + " is not configured"
            );
        }
    }
}
