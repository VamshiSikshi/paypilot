package com.paypilot.backend.domain.audit;

import com.paypilot.backend.domain.audit.dto.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    AuditLogResponse createAuditLog(
            Long merchantId,
            Long userId,
            String eventType,
            String resourceType,
            Long resourceId,
            String description,
            String metadata,
            String ipAddress,
            String userAgent
    );

    List<AuditLogResponse> getAuditLogs(Long merchantId);
}