package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "razorpay_connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_razorpay_connections_merchant",
                        columnNames = "merchant_id"
                ),
                @UniqueConstraint(
                        name = "uq_razorpay_connections_external_account",
                        columnNames = "external_account_id"
                )
        }
)
public class RazorpayConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "external_account_id", nullable = false, length = 100)
    private String externalAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RazorpayConnectionStatus status;

    @Column(name = "access_token_reference", length = 255)
    private String accessTokenReference;

    @Column(name = "refresh_token_reference", length = 255)
    private String refreshTokenReference;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "granted_scopes", columnDefinition = "TEXT")
    private String grantedScopes;

    @Column(name = "last_connected_at")
    private Instant lastConnectedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RazorpayConnection() {
    }

    /**
     * Creates a connection after the Razorpay authorization flow
     * has been successfully verified.
     */
    public RazorpayConnection(Merchant merchant, String externalAccountId) {
        this.merchant = Objects.requireNonNull(merchant, "Merchant is required");

        if (externalAccountId == null || externalAccountId.isBlank()) {
            throw new IllegalArgumentException(
                    "External account ID must not be blank"
            );
        }

        this.externalAccountId = externalAccountId;
        this.status = RazorpayConnectionStatus.CONNECTED;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastConnectedAt = now;
    }

    public void updateTokenReferences(
            String accessTokenReference,
            String refreshTokenReference,
            Instant tokenExpiresAt,
            String grantedScopes
    ) {
        if (accessTokenReference == null || accessTokenReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token reference must not be blank"
            );
        }

        this.accessTokenReference = accessTokenReference;
        this.refreshTokenReference = refreshTokenReference;
        this.tokenExpiresAt = tokenExpiresAt;
        this.grantedScopes = grantedScopes;

        Instant now = Instant.now();
        this.status = RazorpayConnectionStatus.CONNECTED;
        this.lastConnectedAt = now;
        this.updatedAt = now;
    }

    public void markDisconnected() {
        changeStatus(RazorpayConnectionStatus.DISCONNECTED);
    }

    public void markExpired() {
        changeStatus(RazorpayConnectionStatus.EXPIRED);
    }

    public void markError() {
        changeStatus(RazorpayConnectionStatus.ERROR);
    }

    public void recordSuccessfulSync(Instant syncedAt) {
        this.lastSyncedAt = Objects.requireNonNull(
                syncedAt, "Sync timestamp is required"
        );
        this.updatedAt = syncedAt;
    }

    private void changeStatus(RazorpayConnectionStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public RazorpayConnectionStatus getStatus() {
        return status;
    }

    public String getAccessTokenReference() {
        return accessTokenReference;
    }

    public String getRefreshTokenReference() {
        return refreshTokenReference;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public String getGrantedScopes() {
        return grantedScopes;
    }

    public Instant getLastConnectedAt() {
        return lastConnectedAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}