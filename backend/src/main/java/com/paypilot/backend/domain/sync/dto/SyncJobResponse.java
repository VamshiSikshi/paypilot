package com.paypilot.backend.domain.sync.dto;

import com.paypilot.backend.domain.sync.SyncJob;
import com.paypilot.backend.domain.sync.SyncJobStatus;
import com.paypilot.backend.domain.sync.SyncResourceType;
import com.paypilot.backend.domain.sync.SyncTriggerType;

import java.time.Instant;

public record SyncJobResponse(
        Long id,
        Long merchantId,
        SyncResourceType resourceType,
        SyncJobStatus status,
        SyncTriggerType triggerType,
        Instant startedAt,
        Instant completedAt,
        Integer recordsProcessed,
        Integer recordsCreated,
        Integer recordsUpdated,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
    public static SyncJobResponse from(SyncJob syncJob) {
        return new SyncJobResponse(
                syncJob.getId(),
                syncJob.getMerchant().getId(),
                syncJob.getResourceType(),
                syncJob.getStatus(),
                syncJob.getTriggerType(),
                syncJob.getStartedAt(),
                syncJob.getCompletedAt(),
                syncJob.getRecordsProcessed(),
                syncJob.getRecordsCreated(),
                syncJob.getRecordsUpdated(),
                syncJob.getErrorMessage(),
                syncJob.getCreatedAt(),
                syncJob.getUpdatedAt()
        );
    }
}