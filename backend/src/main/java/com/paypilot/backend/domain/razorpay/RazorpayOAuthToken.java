package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "razorpay_oauth_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_razorpay_oauth_tokens_merchant",
                        columnNames = "merchant_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_razorpay_oauth_tokens_updated_at",
                        columnList = "updated_at"
                )
        }
)
public class RazorpayOAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "access_token_ciphertext", nullable = false,
            columnDefinition = "TEXT")
    private String accessTokenCiphertext;

    @Column(name = "refresh_token_ciphertext",
            columnDefinition = "TEXT")
    private String refreshTokenCiphertext;

    @Column(name = "encryption_key_version", nullable = false, length = 50)
    private String encryptionKeyVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RazorpayOAuthToken() {
    }

    public RazorpayOAuthToken(
            Merchant merchant,
            String accessTokenCiphertext,
            String refreshTokenCiphertext,
            String encryptionKeyVersion
    ) {
        this.merchant = Objects.requireNonNull(
                merchant, "Merchant is required"
        );
        this.createdAt = Instant.now();
        updateEncryptedTokens(
                accessTokenCiphertext,
                refreshTokenCiphertext,
                encryptionKeyVersion
        );
    }

    public void updateEncryptedTokens(
            String accessTokenCiphertext,
            String refreshTokenCiphertext,
            String encryptionKeyVersion
    ) {
        if (accessTokenCiphertext == null
                || accessTokenCiphertext.isBlank()) {
            throw new IllegalArgumentException(
                    "Encrypted access token must not be blank"
            );
        }

        if (encryptionKeyVersion == null
                || encryptionKeyVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Encryption key version must not be blank"
            );
        }

        this.accessTokenCiphertext = accessTokenCiphertext;
        this.refreshTokenCiphertext = refreshTokenCiphertext;
        this.encryptionKeyVersion = encryptionKeyVersion;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getAccessTokenCiphertext() {
        return accessTokenCiphertext;
    }

    public String getRefreshTokenCiphertext() {
        return refreshTokenCiphertext;
    }

    public String getEncryptionKeyVersion() {
        return encryptionKeyVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}