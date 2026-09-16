package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class AIToolExecutionRepositoryTest {

    @Autowired
    private AIToolExecutionRepository toolExecutionRepository;

    @Autowired
    private AIConversationRepository conversationRepository;

    @Autowired
    private AIMessageRepository messageRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindToolExecutionByMerchantIdAndToolCallId() {
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

        AIMessage message = messageRepository.save(
                new AIMessage(
                        conversation,
                        merchant,
                        AIMessageRole.ASSISTANT,
                        "I will analyze the failed payments.",
                        AIMessageType.TOOL_CALL
                )
        );

        AIToolExecution execution = toolExecutionRepository.save(
                new AIToolExecution(
                        merchant,
                        conversation,
                        message,
                        "analyze_failed_payments",
                        "call_test_123",
                        "{\"status\":\"FAILED\"}"
                )
        );

        var result = toolExecutionRepository
                .findByMerchantIdAndToolCallId(
                        merchant.getId(),
                        "call_test_123"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(execution.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getConversation().getId())
                .isEqualTo(conversation.getId());
        assertThat(result.get().getMessage().getId())
                .isEqualTo(message.getId());
        assertThat(result.get().getToolName())
                .isEqualTo("analyze_failed_payments");
        assertThat(result.get().getToolCallId())
                .isEqualTo("call_test_123");
        assertThat(result.get().getStatus())
                .isEqualTo(AIToolExecutionStatus.PENDING);
        assertThat(result.get().getInput())
                .isEqualTo("{\"status\":\"FAILED\"}");
        assertThat(result.get().getOutput())
                .isNull();
        assertThat(result.get().getErrorMessage())
                .isNull();
        assertThat(result.get().getStartedAt())
                .isNull();
        assertThat(result.get().getCompletedAt())
                .isNull();
        assertThat(result.get().getDurationMs())
                .isNull();
        assertThat(result.get().getCreatedAt())
                .isNotNull();
    }
}