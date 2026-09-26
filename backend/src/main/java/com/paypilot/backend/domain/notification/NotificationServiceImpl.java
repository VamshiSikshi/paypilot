package com.paypilot.backend.domain.notification;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.notification.dto.NotificationResponse;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationResponse createNotification(
            Long merchantId,
            Long userId,
            NotificationType notificationType,
            NotificationSeverity severity,
            String title,
            String message,
            NotificationResourceType resourceType,
            Long resourceId,
            String metadata
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Merchant not found: " + merchantId));

        User user = userRepository.findByMerchantIdAndId(merchantId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + userId));

        Notification notification = new Notification(
                merchant,
                user,
                notificationType,
                severity,
                title,
                message,
                resourceType,
                resourceId,
                metadata
        );

        return NotificationResponse.from(
                notificationRepository.save(notification)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            Long merchantId,
            Long userId
    ) {
        verifyUserBelongsToMerchant(userId, merchantId);

        return notificationRepository
                .findByUserIdAndMerchantIdOrderByCreatedAtDesc(userId, merchantId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            Long merchantId,
            Long userId
    ) {
        verifyUserBelongsToMerchant(userId, merchantId);

        return notificationRepository
                .findByUserIdAndMerchantIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        merchantId,
                        NotificationStatus.UNREAD
                )
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(
            Long merchantId,
            Long userId,
            Long notificationId
    ) {
        Notification notification = findUserNotification(
                notificationId,
                userId,
                merchantId
        );

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }

    @Override
    public NotificationResponse archiveNotification(
            Long merchantId,
            Long userId,
            Long notificationId
    ) {
        Notification notification = findUserNotification(
                notificationId,
                userId,
                merchantId
        );

        notification.archive();

        return NotificationResponse.from(notification);
    }

    private void verifyUserBelongsToMerchant(
            Long userId,
            Long merchantId
    ) {
        userRepository.findByMerchantIdAndId(merchantId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + userId));
    }

    private Notification findUserNotification(
            Long notificationId,
            Long userId,
            Long merchantId
    ) {
        return notificationRepository
                .findByIdAndUserIdAndMerchantId(
                        notificationId,
                        userId,
                        merchantId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found: " + notificationId
                        ));
    }
}