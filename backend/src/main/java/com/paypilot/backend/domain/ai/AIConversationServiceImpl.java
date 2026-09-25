package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.ai.dto.AIConversationResponse;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AIConversationServiceImpl implements AIConversationService {

    private final AIConversationRepository conversationRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    public AIConversationServiceImpl(
            AIConversationRepository conversationRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AIConversationResponse createConversation(
            Long merchantId,
            Long userId,
            String title
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Merchant not found")
                );

        User user = userRepository
                .findByMerchantIdAndId(merchantId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        AIConversation conversation =
                new AIConversation(merchant, user, title);

        return toResponse(
                conversationRepository.save(conversation)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AIConversationResponse getConversation(
            Long merchantId,
            Long conversationId
    ) {
        AIConversation conversation =
                conversationRepository
                        .findByMerchantIdAndId(
                                merchantId,
                                conversationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "AI conversation not found"
                                )
                        );

        return toResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIConversationResponse> getConversations(
            Long merchantId
    ) {
        return conversationRepository
                .findByMerchantIdOrderByUpdatedAtDesc(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AIConversationResponse archiveConversation(
            Long merchantId,
            Long conversationId
    ) {
        AIConversation conversation =
                conversationRepository
                        .findByMerchantIdAndId(
                                merchantId,
                                conversationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "AI conversation not found"
                                )
                        );

        conversation.archive();

        return toResponse(conversation);
    }

    private AIConversationResponse toResponse(
            AIConversation conversation
    ) {
        return new AIConversationResponse(
                conversation.getId(),
                conversation.getMerchant().getId(),
                conversation.getUser().getId(),
                conversation.getTitle(),
                conversation.getStatus(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getLastMessageAt()
        );
    }
}