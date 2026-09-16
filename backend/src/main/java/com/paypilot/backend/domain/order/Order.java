package com.paypilot.backend.domain.order;

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
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 100)
    private String razorpayOrderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(length = 255)
    private String receipt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "amount_due", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Column(nullable = false)
    private Integer attempts;

    @Column
    private String notes;

    protected Order() {
    }

    public Order(
            Merchant merchant,
            String razorpayOrderId,
            BigDecimal amount,
            String currency,
            String receipt
    ) {
        this.merchant = merchant;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.status = OrderStatus.CREATED;
        this.receipt = receipt;
        this.amountPaid = BigDecimal.ZERO;
        this.amountDue = amount;
        this.attempts = 0;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getReceipt() {
        return receipt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public String getNotes() {
        return notes;
    }
}