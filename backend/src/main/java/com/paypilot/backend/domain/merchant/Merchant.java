package com.paypilot.backend.domain.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razorpay_account_id", nullable = false, unique = true, length = 100)
    private String razorpayAccountId;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MerchantStatus status;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Merchant() {
    }

    public Merchant(
            String razorpayAccountId,
            String businessName,
            String country,
            String defaultCurrency,
            String timezone
    ) {
        this.razorpayAccountId = razorpayAccountId;
        this.businessName = businessName;
        this.country = country;
        this.defaultCurrency = defaultCurrency;
        this.timezone = timezone;
        this.status = MerchantStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getRazorpayAccountId() {
        return razorpayAccountId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getCountry() {
        return country;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getTimezone() {
        return timezone;
    }

    public MerchantStatus getStatus() {
        return status;
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


    public void updateStatus(MerchantStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}