package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paypilot.backend.domain.audit.AuditLogService;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RazorpayConnectionServiceImpl
        implements RazorpayConnectionService {

    private final RazorpayConnectionRepository connectionRepository;
    private final MerchantRepository merchantRepository;
    private final AuditLogService auditLogService;

    public RazorpayConnectionServiceImpl(
            RazorpayConnectionRepository connectionRepository,
            MerchantRepository merchantRepository,
            AuditLogService auditLogService
    ) {
        this.connectionRepository = Objects.requireNonNull(connectionRepository);
        this.merchantRepository = Objects.requireNonNull(merchantRepository);
        this.auditLogService = Objects.requireNonNull(auditLogService);
    }

    @Override
    public RazorpayConnection getConnection(Long merchantId) {
        return findConnection(merchantId);
    }

    @Override
    @Transactional
    public RazorpayConnection disconnect(Long merchantId, Long actorUserId) {
        RazorpayConnection connection = findConnection(merchantId);
        RazorpayConnectionStatus previousStatus = connection.getStatus();

        connection.markDisconnected();

        if (previousStatus != RazorpayConnectionStatus.DISCONNECTED) {
            auditLogService.createAuditLog(
                    merchantId,
                    actorUserId,
                    "RAZORPAY_DISCONNECTED",
                    "RAZORPAY_CONNECTION",
                    connection.getId(),
                    "Razorpay connection disconnected",
                    "{\"previousStatus\":\"" + previousStatus
                            + "\",\"newStatus\":\"DISCONNECTED\"}",
                    null,
                    null
            );
        }

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

        Optional<RazorpayConnection> existingConnection =
                connectionRepository.findByMerchantId(merchantId);

        boolean connectionExists = existingConnection.isPresent();

        RazorpayConnection connection = existingConnection.orElseGet(
                () -> new RazorpayConnection(merchant, externalAccountId)
        );

        RazorpayConnectionStatus previousStatus = connectionExists
                ? connection.getStatus()
                : null;

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

        RazorpayConnection savedConnection = connectionRepository.save(connection);

        String eventType = connectionExists
                ? "RAZORPAY_CONNECTION_UPDATED"
                : "RAZORPAY_CONNECTED";

        String description = connectionExists
                ? "Razorpay connection updated"
                : "Razorpay account connected";

        auditLogService.createAuditLog(
                merchantId,
                null,
                eventType,
                "RAZORPAY_CONNECTION",
                savedConnection.getId(),
                description,
                connectionExists
                        ? "{\"previousStatus\":\"" + previousStatus
                        + "\",\"newStatus\":\"CONNECTED\"}"
                        : "{\"previousStatus\":null,\"newStatus\":\"CONNECTED\"}",
                null,
                null
        );
        return savedConnection;
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