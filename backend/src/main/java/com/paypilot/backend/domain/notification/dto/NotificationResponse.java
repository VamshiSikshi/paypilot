package com.paypilot.backend.domain.notification.dto;

import com.paypilot.backend.domain.notification.Notification;
import com.paypilot.backend.domain.notification.NotificationResourceType;
import com.paypilot.backend.domain.notification.NotificationSeverity;
import com.paypilot.backend.domain.notification.NotificationStatus;
import com.paypilot.backend.domain.notification.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long merchantId,
        Long userId,
        NotificationType notificationType,
        NotificationSeverity severity,
        String title,
        String message,
        NotificationStatus status,
        NotificationResourceType resourceType,
        Long resourceId,
        String metadata,
        Instant readAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMerchant().getId(),
                notification.getUser().getId(),
                notification.getNotificationType(),
                notification.getSeverity(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getMetadata(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}