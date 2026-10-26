package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "razorpay_oauth_states",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_razorpay_oauth_states_state_hash",
                        columnNames = "state_hash"
                )
        },
        indexes = {
                @Index(
                        name = "idx_razorpay_oauth_states_merchant",
                        columnList = "merchant_id"
                ),
                @Index(
                        name = "idx_razorpay_oauth_states_expiry",
                        columnList = "expires_at"
                )
        }
)
public class RazorpayOAuthState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "state_hash", nullable = false, length = 64, unique = true)
    private String stateHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RazorpayOAuthState() {
    }

    public RazorpayOAuthState(
            Merchant merchant,
            String stateHash,
            Instant expiresAt
    ) {
        this.merchant = Objects.requireNonNull(
                merchant, "Merchant is required"
        );

        if (stateHash == null || !stateHash.matches("[a-fA-F0-9]{64}")) {
            throw new IllegalArgumentException(
                    "State hash must be a 64-character hexadecimal SHA-256 hash"
            );
        }

        this.stateHash = stateHash.toLowerCase();

        this.expiresAt = Objects.requireNonNull(
                expiresAt, "Expiry timestamp is required"
        );

        this.createdAt = Instant.now();

        if (!this.expiresAt.isAfter(this.createdAt)) {
            throw new IllegalArgumentException(
                    "Expiry timestamp must be in the future"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getStateHash() {
        return stateHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}