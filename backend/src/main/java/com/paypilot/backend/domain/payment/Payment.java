package com.paypilot.backend.domain.payment;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.order.Order;
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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "razorpay_payment_id", nullable = false, unique = true, length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(length = 50)
    private String method;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_contact", length = 30)
    private String customerContact;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_description", length = 500)
    private String errorDescription;

    @Column(name = "error_source", length = 100)
    private String errorSource;

    @Column(name = "error_reason", length = 255)
    private String errorReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tax;

    @Column(name = "amount_refunded", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountRefunded;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 30)
    private RefundStatus refundStatus;

    @Column(nullable = false)
    private Boolean captured;

    @Column(nullable = false)
    private Boolean international;

    @Column(length = 500)
    private String description;

    @Column
    private String notes;

    protected Payment() {
    }

    public Payment(
            Merchant merchant,
            String razorpayPaymentId,
            String razorpayOrderId,
            BigDecimal amount,
            String currency,
            String method
    ) {
        this.merchant = merchant;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.status = PaymentStatus.CREATED;
        this.fee = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.amountRefunded = BigDecimal.ZERO;
        this.refundStatus = RefundStatus.NOT_REFUNDED;
        this.captured = false;
        this.international = false;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public Merchant getMerchant() { return merchant; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getMethod() { return method; }
    public Instant getCapturedAt() { return capturedAt; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerContact() { return customerContact; }
    public String getErrorCode() { return errorCode; }
    public String getErrorDescription() { return errorDescription; }
    public String getErrorSource() { return errorSource; }
    public String getErrorReason() { return errorReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public Order getOrder() { return order; }
    public BigDecimal getFee() { return fee; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getAmountRefunded() { return amountRefunded; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public Boolean getCaptured() { return captured; }
    public Boolean getInternational() { return international; }
    public String getDescription() { return description; }
    public String getNotes() { return notes; }
}