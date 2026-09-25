package com.paypilot.backend.domain.action;

import com.paypilot.backend.domain.action.dto.ActionExecutionResponse;
import com.paypilot.backend.domain.ai.AIConversation;
import com.paypilot.backend.domain.ai.AIConversationRepository;
import com.paypilot.backend.domain.ai.AIMessage;
import com.paypilot.backend.domain.ai.AIMessageRepository;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionExecutionServiceImplTest {

    @Mock
    private ActionExecutionRepository actionExecutionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIConversationRepository conversationRepository;

    @Mock
    private AIMessageRepository messageRepository;

    @InjectMocks
    private ActionExecutionServiceImpl actionExecutionService;

    private Merchant merchant;
    private User user;

    @BeforeEach
    void setUp() {
        merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        user = new User(
                merchant,
                "owner@test.com",
                "Test Owner",
                UserRole.OWNER
        );
    }

    @Test
    void shouldCreateExecutionWithoutConversationOrMessage() {
        ActionExecution execution = new ActionExecution(
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
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.of(user));
        when(actionExecutionRepository.findByMerchantIdAndIdempotencyKey(
                1L,
                "refund-payment-100"
        )).thenReturn(Optional.empty());
        when(actionExecutionRepository.save(any(ActionExecution.class)))
                .thenReturn(execution);

        ActionExecutionResponse result =
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        null,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                );

        assertThat(result.actionType())
                .isEqualTo(ActionType.REFUND);
        assertThat(result.targetType())
                .isEqualTo(ActionTargetType.PAYMENT);
        assertThat(result.targetId())
                .isEqualTo(100L);
        assertThat(result.status())
                .isEqualTo(ActionExecutionStatus.CONFIRMATION_REQUIRED);
        assertThat(result.confirmationRequired())
                .isTrue();
        assertThat(result.idempotencyKey())
                .isEqualTo("refund-payment-100");

        verify(actionExecutionRepository)
                .save(any(ActionExecution.class));
    }

    @Test
    void shouldRejectExecutionCreationWhenMerchantDoesNotExist() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        null,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Merchant not found");

        verify(userRepository, never())
                .findByMerchantIdAndId(anyLong(), anyLong());
        verify(actionExecutionRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectExecutionCreationWhenUserDoesNotExist() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        null,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(actionExecutionRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnExistingExecutionForDuplicateIdempotencyKey() {
        ActionExecution existingExecution = new ActionExecution(
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
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.of(user));
        when(actionExecutionRepository.findByMerchantIdAndIdempotencyKey(
                1L,
                "refund-payment-100"
        )).thenReturn(Optional.of(existingExecution));

        ActionExecutionResponse result =
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        null,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                );

        assertThat(result.idempotencyKey())
                .isEqualTo("refund-payment-100");

        verify(actionExecutionRepository, never())
                .save(any(ActionExecution.class));
    }

    @Test
    void shouldRejectExecutionCreationWhenConversationDoesNotExist() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        10L,
                        null,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("AI conversation not found");

        verify(actionExecutionRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectExecutionCreationWhenMessageDoesNotExist() {
        AIConversation conversation = mock(AIConversation.class);

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(conversation));
        when(messageRepository
                .findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
                        1L,
                        10L
                ))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        10L,
                        20L,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("AI message not found");

        verify(actionExecutionRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectExecutionCreationWhenMessageIsProvidedWithoutConversation() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(userRepository.findByMerchantIdAndId(1L, 2L))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                actionExecutionService.createExecution(
                        1L,
                        2L,
                        null,
                        20L,
                        ActionType.REFUND,
                        ActionTargetType.PAYMENT,
                        100L,
                        true,
                        "{\"amount\":2500}",
                        "refund-payment-100"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conversation is required when message is provided");

        verify(conversationRepository, never())
                .findByMerchantIdAndId(anyLong(), anyLong());
        verify(messageRepository, never())
                .findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
                        anyLong(),
                        any()
                );
        verify(actionExecutionRepository, never())
                .save(any());
    }

    @Test
    void shouldGetExecution() {
        ActionExecution execution = new ActionExecution(
                merchant,
                user,
                null,
                null,
                ActionType.SYNC,
                ActionTargetType.SETTLEMENT,
                200L,
                false,
                "{\"syncType\":\"SETTLEMENT\"}",
                "sync-settlement-200"
        );

        when(actionExecutionRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(execution));

        ActionExecutionResponse result =
                actionExecutionService.getExecution(1L, 10L);

        assertThat(result.actionType())
                .isEqualTo(ActionType.SYNC);
        assertThat(result.targetType())
                .isEqualTo(ActionTargetType.SETTLEMENT);
        assertThat(result.targetId())
                .isEqualTo(200L);
        assertThat(result.confirmationRequired())
                .isFalse();

        verify(actionExecutionRepository)
                .findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void shouldRejectGettingExecutionWhenExecutionDoesNotExist() {
        when(actionExecutionRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                actionExecutionService.getExecution(1L, 10L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Action execution not found");
    }

    @Test
    void shouldGetExecutionByIdempotencyKey() {
        ActionExecution execution = new ActionExecution(
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
        );

        when(actionExecutionRepository
                .findByMerchantIdAndIdempotencyKey(
                        1L,
                        "refund-payment-100"
                ))
                .thenReturn(Optional.of(execution));

        ActionExecutionResponse result =
                actionExecutionService.getExecutionByIdempotencyKey(
                        1L,
                        "refund-payment-100"
                );

        assertThat(result.idempotencyKey())
                .isEqualTo("refund-payment-100");
        assertThat(result.targetId())
                .isEqualTo(100L);

        verify(actionExecutionRepository)
                .findByMerchantIdAndIdempotencyKey(
                        1L,
                        "refund-payment-100"
                );
    }

    @Test
    void shouldRejectGettingExecutionByIdempotencyKeyWhenExecutionDoesNotExist() {
        when(actionExecutionRepository
                .findByMerchantIdAndIdempotencyKey(
                        1L,
                        "refund-payment-100"
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                actionExecutionService.getExecutionByIdempotencyKey(
                        1L,
                        "refund-payment-100"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Action execution not found");
    }
}