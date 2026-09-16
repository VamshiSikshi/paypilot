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
class AIConversationRepositoryTest {

    @Autowired
    private AIConversationRepository conversationRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindConversationByMerchantIdAndId() {
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

        var result = conversationRepository.findByMerchantIdAndId(
                merchant.getId(),
                conversation.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(conversation.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getUser().getId())
                .isEqualTo(user.getId());
        assertThat(result.get().getTitle())
                .isEqualTo("Payment Analysis");
        assertThat(result.get().getStatus())
                .isEqualTo(AIConversationStatus.ACTIVE);
        assertThat(result.get().getCreatedAt())
                .isNotNull();
        assertThat(result.get().getUpdatedAt())
                .isNotNull();
        assertThat(result.get().getLastMessageAt())
                .isNull();
    }
}