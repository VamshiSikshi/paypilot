package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.ai.dto.AIConversationResponse;

import java.util.List;

public interface AIConversationService {

    AIConversationResponse createConversation(
            Long merchantId,
            Long userId,
            String title
    );

    AIConversationResponse getConversation(
            Long merchantId,
            Long conversationId
    );

    List<AIConversationResponse> getConversations(
            Long merchantId
    );

    AIConversationResponse archiveConversation(
            Long merchantId,
            Long conversationId
    );
}