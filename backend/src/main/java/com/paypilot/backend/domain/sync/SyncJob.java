package com.paypilot.backend.domain.sync;

import com.paypilot.backend.domain.merchant.Merchant;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sync_jobs")
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private SyncResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private SyncTriggerType triggerType;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed;

    @Column(name = "records_created", nullable = false)
    private Integer recordsCreated;

    @Column(name = "records_updated", nullable = false)
    private Integer recordsUpdated;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SyncJob() {
    }

    public SyncJob(
            Merchant merchant,
            SyncResourceType resourceType,
            SyncTriggerType triggerType
    ) {
        this.merchant = merchant;
        this.resourceType = resourceType;
        this.status = SyncJobStatus.PENDING;
        this.triggerType = triggerType;
        this.recordsProcessed = 0;
        this.recordsCreated = 0;
        this.recordsUpdated = 0;

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

    public SyncResourceType getResourceType() {
        return resourceType;
    }

    public SyncJobStatus getStatus() {
        return status;
    }

    public SyncTriggerType getTriggerType() {
        return triggerType;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public Integer getRecordsCreated() {
        return recordsCreated;
    }

    public Integer getRecordsUpdated() {
        return recordsUpdated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}