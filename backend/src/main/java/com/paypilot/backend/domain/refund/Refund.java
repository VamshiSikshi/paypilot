package com.paypilot.backend.domain.refund;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.payment.Payment;
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
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "razorpay_refund_id", nullable = false, unique = true, length = 100)
    private String razorpayRefundId;

    @Column(name = "razorpay_payment_id", nullable = false, length = 100)
    private String razorpayPaymentId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status;

    @Column(name = "speed_requested", length = 50)
    private String speedRequested;

    @Column(name = "speed_processed", length = 50)
    private String speedProcessed;

    @Column
    private String notes;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    protected Refund() {
    }

    public Refund(
            Merchant merchant,
            Payment payment,
            String razorpayRefundId,
            String razorpayPaymentId,
            BigDecimal amount,
            String currency
    ) {
        this.merchant = merchant;
        this.payment = payment;
        this.razorpayRefundId = razorpayRefundId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.amount = amount;
        this.currency = currency;
        this.status = RefundStatus.CREATED;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public Merchant getMerchant() { return merchant; }
    public Payment getPayment() { return payment; }
    public String getRazorpayRefundId() { return razorpayRefundId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public RefundStatus getStatus() { return status; }
    public String getSpeedRequested() { return speedRequested; }
    public String getSpeedProcessed() { return speedProcessed; }
    public String getNotes() { return notes; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
}