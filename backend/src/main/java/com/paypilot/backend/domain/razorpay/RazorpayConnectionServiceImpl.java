package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RazorpayConnectionServiceImpl
        implements RazorpayConnectionService {

    private final RazorpayConnectionRepository connectionRepository;
    private final MerchantRepository merchantRepository;

    public RazorpayConnectionServiceImpl(
            RazorpayConnectionRepository connectionRepository,
            MerchantRepository merchantRepository
    ) {
        this.connectionRepository = connectionRepository;
        this.merchantRepository = merchantRepository;
    }

    @Override
    public RazorpayConnection getConnection(Long merchantId) {
        return findConnection(merchantId);
    }

    @Override
    @Transactional
    public RazorpayConnection disconnect(Long merchantId) {
        RazorpayConnection connection = findConnection(merchantId);
        connection.markDisconnected();
        return connection;
    }

    @Override
    @Transactional
    public RazorpayConnection recordSuccessfulSync(
            Long merchantId,
            Instant syncedAt
    ) {
        Objects.requireNonNull(syncedAt, "Sync timestamp is required");

        RazorpayConnection connection = findConnection(merchantId);
        connection.recordSuccessfulSync(syncedAt);
        return connection;
    }

    @Override
    @Transactional
    public RazorpayConnection connectAuthorizedAccount(
            Long merchantId,
            String externalAccountId,
            String accessTokenReference,
            String refreshTokenReference,
            Instant tokenExpiresAt,
            String grantedScopes
    ) {
        Objects.requireNonNull(merchantId, "Merchant ID is required");

        if (externalAccountId == null || externalAccountId.isBlank()) {
            throw new IllegalArgumentException(
                    "External account ID must not be blank"
            );
        }

        if (accessTokenReference == null || accessTokenReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token reference must not be blank"
            );
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        RazorpayConnection connection = connectionRepository
                .findByMerchantId(merchantId)
                .orElseGet(() -> new RazorpayConnection(
                        merchant, externalAccountId
                ));

        if (!externalAccountId.equals(connection.getExternalAccountId())) {
            throw new IllegalStateException(
                    "Merchant already has a connection to a different "
                            + "Razorpay account; disconnect it before switching accounts"
            );
        }

        connection.updateTokenReferences(
                accessTokenReference,
                refreshTokenReference,
                tokenExpiresAt,
                grantedScopes
        );

        return connectionRepository.save(connection);
    }

    private RazorpayConnection findConnection(Long merchantId) {
        Objects.requireNonNull(merchantId, "Merchant ID is required");

        return connectionRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Razorpay connection not found for merchant: "
                                + merchantId
                ));
    }
}