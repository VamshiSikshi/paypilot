package com.paypilot.backend.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIMessageRepository extends JpaRepository<AIMessage, Long> {

    List<AIMessage> findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
            Long merchantId,
            Long conversationId
    );
}