package com.paypilot.backend.domain.razorpay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;

@Service
public class RazorpayOAuthConnectionServiceImpl
        implements RazorpayOAuthConnectionService {

    private final RazorpayTokenStorageService tokenStorageService;
    private final RazorpayConnectionService connectionService;

    public RazorpayOAuthConnectionServiceImpl(
            RazorpayTokenStorageService tokenStorageService,
            RazorpayConnectionService connectionService
    ) {
        this.tokenStorageService = Objects.requireNonNull(tokenStorageService);
        this.connectionService = Objects.requireNonNull(connectionService);
    }

    @Override
    @Transactional
    public RazorpayConnection completeAuthorization(
            Long merchantId,
            String externalAccountId,
            String accessToken,
            String refreshToken,
            Instant tokenExpiresAt,
            String grantedScopes
    ) {
        Objects.requireNonNull(merchantId, "Merchant ID is required");

        if (!StringUtils.hasText(externalAccountId)) {
            throw new IllegalArgumentException(
                    "External account ID is required"
            );
        }

        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalArgumentException("Access token is required");
        }

        String accessTokenReference = tokenStorageService.storeTokens(
                merchantId,
                accessToken,
                refreshToken
        );

        String refreshTokenReference = StringUtils.hasText(refreshToken)
                ? accessTokenReference
                : null;

        return connectionService.connectAuthorizedAccount(
                merchantId,
                externalAccountId,
                accessTokenReference,
                refreshTokenReference,
                tokenExpiresAt,
                grantedScopes
        );
    }
}
