package com.paypilot.backend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AIConversationRepository
        extends JpaRepository<AIConversation, Long> {

    Optional<AIConversation> findByMerchantIdAndId(
            Long merchantId,
            Long id
    );

    List<AIConversation> findByMerchantIdOrderByUpdatedAtDesc(
            Long merchantId
    );
}