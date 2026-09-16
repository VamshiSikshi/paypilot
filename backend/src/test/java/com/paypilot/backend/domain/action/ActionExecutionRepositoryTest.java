package com.paypilot.backend.domain.action;

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
class ActionExecutionRepositoryTest {

    @Autowired
    private ActionExecutionRepository actionExecutionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindActionExecutionByMerchantIdAndIdempotencyKey() {
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

        ActionExecution execution = actionExecutionRepository.save(
                new ActionExecution(
                        merchant,
                        user,
                        null,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        );

        var result = actionExecutionRepository
                .findByMerchantIdAndIdempotencyKey(
                        merchant.getId(),
                        "refund-payment-100"
                );

        assertThat(result).isPresent();

        ActionExecution savedExecution = result.get();

        assertThat(savedExecution.getId())
                .isEqualTo(execution.getId());
        assertThat(savedExecution.getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(savedExecution.getUser().getId())
                .isEqualTo(user.getId());
        assertThat(savedExecution.getConversation())
                .isNull();
        assertThat(savedExecution.getMessage())
                .isNull();
        assertThat(savedExecution.getActionType())
                .isEqualTo(ActionType.REFUND);
        assertThat(savedExecution.getTargetType())
                .isEqualTo(ActionTargetType.PAYMENT);
        assertThat(savedExecution.getTargetId())
                .isEqualTo(100L);
        assertThat(savedExecution.getStatus())
                .isEqualTo(ActionExecutionStatus.CONFIRMATION_REQUIRED);
        assertThat(savedExecution.getConfirmationRequired())
                .isTrue();
        assertThat(savedExecution.getConfirmedAt())
                .isNull();
        assertThat(savedExecution.getRequestedAt())
                .isNotNull();
        assertThat(savedExecution.getStartedAt())
                .isNull();
        assertThat(savedExecution.getCompletedAt())
                .isNull();
        assertThat(savedExecution.getRequestData())
                .isEqualTo("{\"amount\":2500}");
        assertThat(savedExecution.getResultData())
                .isNull();
        assertThat(savedExecution.getErrorMessage())
                .isNull();
        assertThat(savedExecution.getCreatedAt())
                .isNotNull();
        assertThat(savedExecution.getUpdatedAt())
                .isNotNull();
        assertThat(savedExecution.getIdempotencyKey())
                .isEqualTo("refund-payment-100");
    }
}