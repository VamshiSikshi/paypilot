package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIToolServiceImplTest {

    @Test
    void shouldCreateExecution() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        Merchant merchant = mock(Merchant.class);
        AIConversation conversation = mock(AIConversation.class);
        AIMessage message = mock(AIMessage.class);
        AIToolExecution execution = mock(AIToolExecution.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.of(merchant));

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.of(conversation));

        when(messageRepository
                .findByMerchantIdAndConversationIdAndId(10L, 100L, 200L))
                .thenReturn(Optional.of(message));

        when(message.getId()).thenReturn(200L);

        when(toolExecutionRepository.save(any(AIToolExecution.class)))
                .thenReturn(execution);

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.createExecution(
                        10L,
                        100L,
                        200L,
                        "analyze_failed_payments",
                        "call_test_123",
                        "{\"status\":\"FAILED\"}"
                );

        assertSame(execution, result);

        verify(merchantRepository).findById(10L);
        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);
        verify(messageRepository)
                .findByMerchantIdAndConversationIdAndId(
                        10L,
                        100L,
                        200L
                );
        verify(toolExecutionRepository)
                .save(any(AIToolExecution.class));
    }

    @Test
    void shouldRejectExecutionCreationWhenMerchantDoesNotExist() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.empty());

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExecution(
                        10L,
                        100L,
                        200L,
                        "analyze_failed_payments",
                        "call_test_123",
                        "{\"status\":\"FAILED\"}"
                )
        );

        verify(merchantRepository).findById(10L);
        verify(conversationRepository, never())
                .findByMerchantIdAndId(anyLong(), anyLong());
        verify(toolExecutionRepository, never())
                .save(any(AIToolExecution.class));
    }

    @Test
    void shouldRejectExecutionCreationWhenConversationDoesNotExist() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        Merchant merchant = mock(Merchant.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.of(merchant));

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.empty());

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExecution(
                        10L,
                        100L,
                        200L,
                        "analyze_failed_payments",
                        "call_test_123",
                        "{\"status\":\"FAILED\"}"
                )
        );

        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);
        verify(messageRepository, never())
                .findByMerchantIdAndConversationIdOrderByCreatedAtAsc(
                        anyLong(),
                        anyLong()
                );
        verify(toolExecutionRepository, never())
                .save(any(AIToolExecution.class));
    }

    @Test
    void shouldRejectExecutionCreationWhenMessageDoesNotExist() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        Merchant merchant = mock(Merchant.class);
        AIConversation conversation = mock(AIConversation.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.of(merchant));

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.of(conversation));

        when(messageRepository
                .findByMerchantIdAndConversationIdAndId(10L, 100L, 200L))
                .thenReturn(Optional.empty());

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createExecution(
                        10L,
                        100L,
                        200L,
                        "analyze_failed_payments",
                        "call_test_123",
                        "{\"status\":\"FAILED\"}"
                )
        );

        verify(messageRepository)
                .findByMerchantIdAndConversationIdAndId(
                        10L,
                        100L,
                        200L
                );
        verify(toolExecutionRepository, never())
                .save(any(AIToolExecution.class));
    }

    @Test
    void shouldGetExecution() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        AIToolExecution execution = mock(AIToolExecution.class);

        when(toolExecutionRepository.findByMerchantIdAndId(10L, 300L))
                .thenReturn(Optional.of(execution));

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.getExecution(10L, 300L);

        assertSame(execution, result);

        verify(toolExecutionRepository)
                .findByMerchantIdAndId(10L, 300L);
    }

    @Test
    void shouldRejectGettingExecutionWhenExecutionDoesNotExist() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        when(toolExecutionRepository.findByMerchantIdAndId(10L, 300L))
                .thenReturn(Optional.empty());

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getExecution(10L, 300L)
        );

        verify(toolExecutionRepository)
                .findByMerchantIdAndId(10L, 300L);
    }

    @Test
    void shouldGetExecutionByToolCallId() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        AIToolExecution execution = mock(AIToolExecution.class);

        when(toolExecutionRepository
                .findByMerchantIdAndToolCallId(10L, "call_test_123"))
                .thenReturn(Optional.of(execution));

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.getExecutionByToolCallId(
                        10L,
                        "call_test_123"
                );

        assertSame(execution, result);

        verify(toolExecutionRepository)
                .findByMerchantIdAndToolCallId(
                        10L,
                        "call_test_123"
                );
    }

    @Test
    void shouldRejectGettingExecutionByToolCallIdWhenExecutionDoesNotExist() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        when(toolExecutionRepository
                .findByMerchantIdAndToolCallId(10L, "call_test_123"))
                .thenReturn(Optional.empty());

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getExecutionByToolCallId(
                        10L,
                        "call_test_123"
                )
        );

        verify(toolExecutionRepository)
                .findByMerchantIdAndToolCallId(
                        10L,
                        "call_test_123"
                );
    }

    @Test
    void shouldStartExecution() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        AIToolExecution execution = mock(AIToolExecution.class);

        when(toolExecutionRepository.findByMerchantIdAndId(10L, 300L))
                .thenReturn(Optional.of(execution));

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.startExecution(10L, 300L);

        assertSame(execution, result);

        verify(toolExecutionRepository)
                .findByMerchantIdAndId(10L, 300L);
        verify(execution).start();
    }

    @Test
    void shouldCompleteExecution() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        AIToolExecution execution = mock(AIToolExecution.class);

        when(toolExecutionRepository.findByMerchantIdAndId(10L, 300L))
                .thenReturn(Optional.of(execution));

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.completeExecution(
                        10L,
                        300L,
                        "{\"result\":\"success\"}"
                );

        assertSame(execution, result);

        verify(toolExecutionRepository)
                .findByMerchantIdAndId(10L, 300L);
        verify(execution)
                .complete("{\"result\":\"success\"}");
    }

    @Test
    void shouldFailExecution() {
        AIToolExecutionRepository toolExecutionRepository =
                mock(AIToolExecutionRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        AIMessageRepository messageRepository =
                mock(AIMessageRepository.class);

        AIToolExecution execution = mock(AIToolExecution.class);

        when(toolExecutionRepository.findByMerchantIdAndId(10L, 300L))
                .thenReturn(Optional.of(execution));

        AIToolService service =
                new AIToolServiceImpl(
                        toolExecutionRepository,
                        merchantRepository,
                        conversationRepository,
                        messageRepository
                );

        AIToolExecution result =
                service.failExecution(
                        10L,
                        300L,
                        "AI tool failed"
                );

        assertSame(execution, result);

        verify(toolExecutionRepository)
                .findByMerchantIdAndId(10L, 300L);
        verify(execution)
                .fail("AI tool failed");
    }
}