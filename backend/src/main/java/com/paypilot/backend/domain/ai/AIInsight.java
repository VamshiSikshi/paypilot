package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "ai_insights")
public class AIInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "insight_type", nullable = false, length = 50)
    private String insightType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightSource source;

    @JdbcTypeCode(SqlTypes.JSON)
    private String data;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AIInsight() {
    }

    public AIInsight(
            Merchant merchant,
            String insightType,
            InsightSeverity severity,
            String title,
            String description,
            InsightSource source,
            String data,
            Instant validFrom,
            Instant validUntil
    ) {
        this.merchant = merchant;
        this.insightType = insightType;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.status = InsightStatus.ACTIVE;
        this.source = source;
        this.data = data;
        this.validFrom = validFrom;
        this.validUntil = validUntil;

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

    public String getInsightType() {
        return insightType;
    }

    public InsightSeverity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public InsightStatus getStatus() {
        return status;
    }

    public InsightSource getSource() {
        return source;
    }

    public String getData() {
        return data;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}