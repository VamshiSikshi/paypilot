package com.paypilot.backend.domain.settlement;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "razorpay_settlement_id", nullable = false, unique = true, length = 100)
    private String razorpaySettlementId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementStatus status;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "settlement_utr", length = 100)
    private String settlementUtr;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tax;

    protected Settlement() {
    }

    public Settlement(
            Merchant merchant,
            String razorpaySettlementId,
            BigDecimal amount,
            String currency
    ) {
        this.merchant = merchant;
        this.razorpaySettlementId = razorpaySettlementId;
        this.amount = amount;
        this.currency = currency;
        this.status = SettlementStatus.CREATED;
        this.fee = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public Merchant getMerchant() { return merchant; }
    public String getRazorpaySettlementId() { return razorpaySettlementId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public SettlementStatus getStatus() { return status; }
    public Instant getSettledAt() { return settledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public String getSettlementUtr() { return settlementUtr; }
    public BigDecimal getFee() { return fee; }
    public BigDecimal getTax() { return tax; }
}