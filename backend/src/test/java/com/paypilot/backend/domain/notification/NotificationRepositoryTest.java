package com.paypilot.backend.domain.notification;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.paypilot.backend.domain.user.UserRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class NotificationRepositoryTest {

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
                        "razorpay_notification_1",
                        "Notification Merchant 1",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        merchant2 = merchantRepository.save(
                new Merchant(
                        "razorpay_notification_2",
                        "Notification Merchant 2",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        user1 = userRepository.save(
                new User(
                        merchant1,
                        "notification1@test.com",
                        "Notification User 1",
                        UserRole.OWNER
                )
        );

        user2 = userRepository.save(
                new User(
                        merchant2,
                        "notification2@test.com",
                        "Notification User 2",
                        UserRole.OWNER
                )
        );
    }

    @Test
    void shouldFindNotificationsByMerchant() {
        Notification notification1 = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        Notification notification2 = createNotification(
                merchant2,
                user2,
                NotificationType.PAYMENT_FAILED
        );

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        List<Notification> notifications =
                notificationRepository.findByMerchantIdOrderByCreatedAtDesc(
                        merchant1.getId()
                );

        assertEquals(1, notifications.size());
        assertEquals(notification1.getId(), notifications.get(0).getId());
    }

    @Test
    void shouldFindNotificationsByUserAndMerchant() {
        Notification notification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        notificationRepository.save(notification);

        List<Notification> notifications =
                notificationRepository.findByUserIdAndMerchantIdOrderByCreatedAtDesc(
                        user1.getId(),
                        merchant1.getId()
                );

        assertEquals(1, notifications.size());
        assertEquals(notification.getId(), notifications.get(0).getId());
    }

    @Test
    void shouldFilterNotificationsByStatus() {
        Notification unreadNotification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        Notification readNotification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_FAILED
        );
        readNotification.markAsRead();

        notificationRepository.save(unreadNotification);
        notificationRepository.save(readNotification);

        List<Notification> unread =
                notificationRepository
                        .findByUserIdAndMerchantIdAndStatusOrderByCreatedAtDesc(
                                user1.getId(),
                                merchant1.getId(),
                                NotificationStatus.UNREAD
                        );

        List<Notification> read =
                notificationRepository
                        .findByUserIdAndMerchantIdAndStatusOrderByCreatedAtDesc(
                                user1.getId(),
                                merchant1.getId(),
                                NotificationStatus.READ
                        );

        assertEquals(1, unread.size());
        assertEquals(unreadNotification.getId(), unread.get(0).getId());

        assertEquals(1, read.size());
        assertEquals(readNotification.getId(), read.get(0).getId());
    }

    @Test
    void shouldFindNotificationByIdAndMerchant() {
        Notification notification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        notificationRepository.save(notification);

        assertTrue(
                notificationRepository
                        .findByIdAndMerchantId(
                                notification.getId(),
                                merchant1.getId()
                        )
                        .isPresent()
        );
    }

    @Test
    void shouldNotFindNotificationForDifferentMerchant() {
        Notification notification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        notificationRepository.save(notification);

        assertTrue(
                notificationRepository
                        .findByIdAndMerchantId(
                                notification.getId(),
                                merchant2.getId()
                        )
                        .isEmpty()
        );
    }

    @Test
    void shouldFindNotificationByIdUserAndMerchant() {
        Notification notification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        notificationRepository.save(notification);

        assertTrue(
                notificationRepository
                        .findByIdAndUserIdAndMerchantId(
                                notification.getId(),
                                user1.getId(),
                                merchant1.getId()
                        )
                        .isPresent()
        );
    }

    @Test
    void shouldNotFindNotificationForDifferentUser() {
        Notification notification = createNotification(
                merchant1,
                user1,
                NotificationType.PAYMENT_SUCCESS
        );

        notificationRepository.save(notification);

        assertTrue(
                notificationRepository
                        .findByIdAndUserIdAndMerchantId(
                                notification.getId(),
                                user2.getId(),
                                merchant1.getId()
                        )
                        .isEmpty()
        );
    }

    private Notification createNotification(
            Merchant merchant,
            User user,
            NotificationType notificationType
    ) {
        return new Notification(
                merchant,
                user,
                notificationType,
                NotificationSeverity.INFO,
                "Test Notification",
                "Test notification message",
                NotificationResourceType.PAYMENT,
                1L,
                "{\"source\":\"test\"}"
        );
    }
}