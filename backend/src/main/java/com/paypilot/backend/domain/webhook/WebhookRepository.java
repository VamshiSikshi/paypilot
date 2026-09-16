package com.paypilot.backend.domain.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    Optional<Webhook> findByMerchantIdAndEventId(
            Long merchantId,
            String eventId
    );
}