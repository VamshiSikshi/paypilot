package com.paypilot.backend.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<Notification> findByUserIdAndMerchantIdOrderByCreatedAtDesc(
            Long userId,
            Long merchantId
    );

    List<Notification> findByUserIdAndMerchantIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            Long merchantId,
            NotificationStatus status
    );

    Optional<Notification> findByIdAndMerchantId(Long id, Long merchantId);

    Optional<Notification> findByIdAndUserIdAndMerchantId(
            Long id,
            Long userId,
            Long merchantId
    );
}