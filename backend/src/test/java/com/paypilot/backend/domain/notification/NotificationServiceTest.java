package com.paypilot.backend.domain.notification;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.notification.dto.NotificationResponse;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    private Merchant merchant1;
    private Merchant merchant2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        merchant1 = merchantRepository.save(
                new Merchant(
                        "razorpay_notification_service_1",
                        "Notification Service Merchant 1",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        merchant2 = merchantRepository.save(
                new Merchant(
                        "razorpay_notification_service_2",
                        "Notification Service Merchant 2",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        user1 = userRepository.save(
                new User(
                        merchant1,
                        "notification-service-1@test.com",
                        "Notification Service User 1",
                        UserRole.OWNER
                )
        );

        user2 = userRepository.save(
                new User(
                        merchant2,
                        "notification-service-2@test.com",
                        "Notification Service User 2",
                        UserRole.OWNER
                )
        );
    }

    @Test
    void shouldCreateNotification() {
        NotificationResponse response = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_FAILED,
                NotificationSeverity.HIGH,
                "Payment Failed",
                "A payment failed.",
                NotificationResourceType.PAYMENT,
                100L,
                "{\"source\":\"razorpay\"}"
        );

        assertNotNull(response.id());
        assertEquals(merchant1.getId(), response.merchantId());
        assertEquals(user1.getId(), response.userId());
        assertEquals(NotificationType.PAYMENT_FAILED, response.notificationType());
        assertEquals(NotificationSeverity.HIGH, response.severity());
        assertEquals(NotificationStatus.UNREAD, response.status());
        assertEquals(NotificationResourceType.PAYMENT, response.resourceType());
        assertEquals(100L, response.resourceId());
        assertEquals("{\"source\":\"razorpay\"}", response.metadata());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertNull(response.readAt());
    }

    @Test
    void shouldNotCreateNotificationForUserFromDifferentMerchant() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.createNotification(
                        merchant1.getId(),
                        user2.getId(),
                        NotificationType.PAYMENT_SUCCESS,
                        NotificationSeverity.INFO,
                        "Payment Successful",
                        "A payment was successful.",
                        NotificationResourceType.PAYMENT,
                        101L,
                        null
                )
        );

        assertEquals(0, notificationRepository.count());
    }

    @Test
    void shouldGetUserNotifications() {
        notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationSeverity.INFO,
                "Payment Successful",
                "Payment completed.",
                NotificationResourceType.PAYMENT,
                200L,
                null
        );

        notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.REFUND_COMPLETED,
                NotificationSeverity.LOW,
                "Refund Completed",
                "Refund completed.",
                NotificationResourceType.REFUND,
                201L,
                null
        );

        List<NotificationResponse> notifications =
                notificationService.getNotifications(
                        merchant1.getId(),
                        user1.getId()
                );

        assertEquals(2, notifications.size());
        assertTrue(
                notifications.stream()
                        .allMatch(n -> n.merchantId().equals(merchant1.getId()))
        );
        assertTrue(
                notifications.stream()
                        .allMatch(n -> n.userId().equals(user1.getId()))
        );
    }

    @Test
    void shouldGetOnlyUnreadNotifications() {
        NotificationResponse unread = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_FAILED,
                NotificationSeverity.HIGH,
                "Payment Failed",
                "Payment failed.",
                NotificationResourceType.PAYMENT,
                300L,
                null
        );

        NotificationResponse read = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationSeverity.INFO,
                "Payment Successful",
                "Payment completed.",
                NotificationResourceType.PAYMENT,
                301L,
                null
        );

        notificationService.markAsRead(
                merchant1.getId(),
                user1.getId(),
                read.id()
        );

        List<NotificationResponse> notifications =
                notificationService.getUnreadNotifications(
                        merchant1.getId(),
                        user1.getId()
                );

        assertEquals(1, notifications.size());
        assertEquals(unread.id(), notifications.get(0).id());
        assertEquals(NotificationStatus.UNREAD, notifications.get(0).status());
    }

    @Test
    void shouldMarkNotificationAsRead() {
        NotificationResponse created = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationSeverity.INFO,
                "Payment Successful",
                "Payment completed.",
                NotificationResourceType.PAYMENT,
                400L,
                null
        );

        NotificationResponse response = notificationService.markAsRead(
                merchant1.getId(),
                user1.getId(),
                created.id()
        );

        assertEquals(NotificationStatus.READ, response.status());
        assertNotNull(response.readAt());
    }

    @Test
    void shouldArchiveNotification() {
        NotificationResponse created = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.SYSTEM,
                NotificationSeverity.MEDIUM,
                "System Notification",
                "System notification.",
                NotificationResourceType.SYSTEM,
                null,
                null
        );

        NotificationResponse response = notificationService.archiveNotification(
                merchant1.getId(),
                user1.getId(),
                created.id()
        );

        assertEquals(NotificationStatus.ARCHIVED, response.status());
        assertNotNull(response.readAt());
    }

    @Test
    void shouldNotAccessNotificationFromDifferentMerchant() {
        NotificationResponse notification = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationSeverity.INFO,
                "Payment Successful",
                "Payment completed.",
                NotificationResourceType.PAYMENT,
                500L,
                null
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(
                        merchant2.getId(),
                        user2.getId(),
                        notification.id()
                )
        );
    }

    @Test
    void shouldNotAccessNotificationFromDifferentUser() {
        NotificationResponse notification = notificationService.createNotification(
                merchant1.getId(),
                user1.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationSeverity.INFO,
                "Payment Successful",
                "Payment completed.",
                NotificationResourceType.PAYMENT,
                600L,
                null
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(
                        merchant1.getId(),
                        user2.getId(),
                        notification.id()
                )
        );
    }
}