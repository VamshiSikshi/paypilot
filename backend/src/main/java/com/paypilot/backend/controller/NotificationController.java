package com.paypilot.backend.controller;

import com.paypilot.backend.domain.notification.NotificationService;
import com.paypilot.backend.domain.notification.dto.NotificationResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public NotificationController(
            NotificationService notificationService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.notificationService = notificationService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return notificationService.getNotifications(
                user.merchantId(),
                user.userId()
        );
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return notificationService.getUnreadNotifications(
                user.merchantId(),
                user.userId()
        );
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId
    ) {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return notificationService.markAsRead(
                user.merchantId(),
                user.userId(),
                notificationId
        );
    }

    @PatchMapping("/{notificationId}/archive")
    public NotificationResponse archiveNotification(
            @PathVariable Long notificationId
    ) {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return notificationService.archiveNotification(
                user.merchantId(),
                user.userId(),
                notificationId
        );
    }
}
