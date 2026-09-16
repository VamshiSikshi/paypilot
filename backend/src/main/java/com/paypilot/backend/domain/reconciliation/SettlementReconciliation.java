package com.paypilot.backend.domain.reconciliation;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.settlement.Settlement;
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
@Table(name = "settlement_reconciliation")
public class SettlementReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @Column(name = "razorpay_entity_id", nullable = false, length = 100)
    private String razorpayEntityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private ReconciliationTransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal credit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tax;

    @Column(name = "on_hold", nullable = false)
    private Boolean onHold;

    @Column(nullable = false)
    private Boolean settled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(length = 500)
    private String description;

    @Column
    private String notes;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "order_receipt", length = 255)
    private String orderReceipt;

    @Column(length = 50)
    private String method;

    @Column(name = "card_network", length = 50)
    private String cardNetwork;

    @Column(name = "card_issuer", length = 100)
    private String cardIssuer;

    @Column(name = "card_type", length = 50)
    private String cardType;

    @Column(name = "dispute_id", length = 100)
    private String disputeId;

    @Column(name = "settlement_utr", length = 100)
    private String settlementUtr;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    protected SettlementReconciliation() {
    }

    public SettlementReconciliation(
            Merchant merchant,
            Settlement settlement,
            String razorpayEntityId,
            ReconciliationTransactionType transactionType,
            BigDecimal amount,
            String currency
    ) {
        this.merchant = merchant;
        this.settlement = settlement;
        this.razorpayEntityId = razorpayEntityId;
        this.transactionType = transactionType;
        this.debit = BigDecimal.ZERO;
        this.credit = BigDecimal.ZERO;
        this.amount = amount;
        this.currency = currency;
        this.fee = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.onHold = false;
        this.settled = false;

        Instant now = Instant.now();
        this.createdAt = now;
    }

    public Long getId() { return id; }
    public Merchant getMerchant() { return merchant; }
    public Settlement getSettlement() { return settlement; }
    public String getRazorpayEntityId() { return razorpayEntityId; }
    public ReconciliationTransactionType getTransactionType() { return transactionType; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BigDecimal getFee() { return fee; }
    public BigDecimal getTax() { return tax; }
    public Boolean getOnHold() { return onHold; }
    public Boolean getSettled() { return settled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSettledAt() { return settledAt; }
    public String getDescription() { return description; }
    public String getNotes() { return notes; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public String getOrderReceipt() { return orderReceipt; }
    public String getMethod() { return method; }
    public String getCardNetwork() { return cardNetwork; }
    public String getCardIssuer() { return cardIssuer; }
    public String getCardType() { return cardType; }
    public String getDisputeId() { return disputeId; }
    public String getSettlementUtr() { return settlementUtr; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
}