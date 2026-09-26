package com.paypilot.backend.domain.notification;

import com.paypilot.backend.domain.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(
            Long merchantId,
            Long userId,
            NotificationType notificationType,
            NotificationSeverity severity,
            String title,
            String message,
            NotificationResourceType resourceType,
            Long resourceId,
            String metadata
    );

    List<NotificationResponse> getNotifications(
            Long merchantId,
            Long userId
    );

    List<NotificationResponse> getUnreadNotifications(
            Long merchantId,
            Long userId
    );

    NotificationResponse markAsRead(
            Long merchantId,
            Long userId,
            Long notificationId
    );

    NotificationResponse archiveNotification(
            Long merchantId,
            Long userId,
            Long notificationId
    );
}