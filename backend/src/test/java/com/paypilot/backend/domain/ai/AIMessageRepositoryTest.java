package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class AIMessageRepositoryTest {

    @Autowired
    private AIMessageRepository messageRepository;

    @Autowired
    private AIConversationRepository conversationRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindMessagesByMerchantIdAndConversationIdInCreatedOrder() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "owner@test.com",
                        "Test Owner",
                        UserRole.OWNER
                )
        );

        AIConversation conversation = conversationRepository.save(
                new AIConversation(
                        merchant,
                        user,
                        "Payment Analysis"
                )
        );

        AIMessage userMessage = messageRepository.save(
                new AIMessage(
                        conversation,
                        merchant,
                        AIMessageRole.USER,
                        "Why did my payment fail?",
                        AIMessageType.TEXT
                )
        );

        AIMessage assistantMessage = messageRepository.save(
                new AIMessage(
                        conversation,
                        merchant,
                        AIMessageRole.ASSISTANT,
                        "The payment failed because of an authentication error.",
                        AIMessageType.TEXT
                )
        );

        List<AIMessage> result =
                messageRepository.findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
                        merchant.getId(),
                        conversation.getId()
                );

        assertThat(result)
                .hasSize(2)
                .extracting(AIMessage::getId)
                .containsExactly(
                        userMessage.getId(),
                        assistantMessage.getId()
                );

        assertThat(result.get(0).getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get(0).getConversation().getId())
                .isEqualTo(conversation.getId());
        assertThat(result.get(0).getRole())
                .isEqualTo(AIMessageRole.USER);
        assertThat(result.get(0).getContent())
                .isEqualTo("Why did my payment fail?");
        assertThat(result.get(0).getMessageType())
                .isEqualTo(AIMessageType.TEXT);
        assertThat(result.get(0).getCreatedAt())
                .isNotNull();

        assertThat(result.get(1).getRole())
                .isEqualTo(AIMessageRole.ASSISTANT);
        assertThat(result.get(1).getContent())
                .isEqualTo(
                        "The payment failed because of an authentication error."
                );
        assertThat(result.get(1).getMessageType())
                .isEqualTo(AIMessageType.TEXT);
        assertThat(result.get(1).getCreatedAt())
                .isNotNull();
    }
}