package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import com.paypilot.backend.domain.razorpay.dto.RazorpayOAuthTokenResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Objects;

@Service
public class RazorpayOAuthTokenExchangeServiceImpl
        implements RazorpayOAuthTokenExchangeService {

    private final RestClient restClient;
    private final RazorpayOAuthProperties properties;

    public RazorpayOAuthTokenExchangeServiceImpl(
            RestClient.Builder restClientBuilder,
            RazorpayOAuthProperties properties
    ) {
        this.restClient = Objects.requireNonNull(restClientBuilder).build();
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public RazorpayOAuthTokenResponse exchangeAuthorizationCode(String code) {
        requireConfigured(properties.clientId(), "Razorpay OAuth client ID");
        requireConfigured(properties.clientSecret(), "Razorpay OAuth client secret");
        requireConfigured(properties.redirectUri(), "Razorpay OAuth redirect URI");
        requireConfigured(properties.tokenUri(), "Razorpay OAuth token URI");

        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException(
                    "Razorpay authorization code must not be blank"
            );
        }

        URI tokenUri = URI.create(properties.tokenUri());
        if (!"https".equalsIgnoreCase(tokenUri.getScheme())
                || tokenUri.getHost() == null) {
            throw new IllegalStateException(
                    "Razorpay OAuth token URI must be an HTTPS URL"
            );
        }

        TokenExchangeRequest request = new TokenExchangeRequest(
                properties.clientId(),
                properties.clientSecret(),
                "authorization_code",
                properties.redirectUri(),
                code,
                properties.mode()
        );

        try {
            RazorpayOAuthTokenResponse response = restClient.post()
                    .uri(tokenUri)
                    .body(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (httpRequest, httpResponse) -> {
                                throw new IllegalStateException(
                                        "Razorpay OAuth token exchange failed "
                                                + "with HTTP status "
                                                + httpResponse.getStatusCode().value()
                                );
                            }
                    )
                    .body(RazorpayOAuthTokenResponse.class);

            if (response == null
                    || !StringUtils.hasText(response.accessToken())
                    || !StringUtils.hasText(response.razorpayAccountId())) {
                throw new IllegalStateException(
                        "Razorpay OAuth response is missing required token "
                                + "or account information"
                );
            }

            if (response.expiresIn() != null && response.expiresIn() <= 0) {
                throw new IllegalStateException(
                        "Razorpay OAuth response contains an invalid expiry"
                );
            }

            return response;
        } catch (org.springframework.web.client.RestClientException exception) {
            throw new IllegalStateException(
                    "Unable to complete Razorpay OAuth token exchange"
            );
        }
    }

    private void requireConfigured(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " is not configured");
        }
    }

    private record TokenExchangeRequest(
            String client_id,
            String client_secret,
            String grant_type,
            String redirect_uri,
            String code,
            String mode
    ) {
    }
}
