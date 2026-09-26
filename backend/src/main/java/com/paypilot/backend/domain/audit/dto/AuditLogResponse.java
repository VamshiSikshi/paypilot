package com.paypilot.backend.domain.audit.dto;

import com.paypilot.backend.domain.audit.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long merchantId,
        Long userId,
        String eventType,
        String resourceType,
        Long resourceId,
        String description,
        String metadata,
        String ipAddress,
        String userAgent,
        Instant createdAt
) {

    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getMerchant().getId(),
                auditLog.getUser() != null ? auditLog.getUser().getId() : null,
                auditLog.getEventType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getDescription(),
                auditLog.getMetadata(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }
}