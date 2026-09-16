package com.paypilot.backend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AIConversationRepository extends JpaRepository<AIConversation, Long> {

    Optional<AIConversation> findByMerchantIdAndId(
            Long merchantId,
            Long id
    );
}