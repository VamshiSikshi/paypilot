package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.ai.dto.AIConversationResponse;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import com.paypilot.backend.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIConversationServiceImplTest {

    @Test
    void shouldCreateConversation() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);
        AIConversation conversation = mock(AIConversation.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.of(merchant));

        when(userRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(user));

        when(conversationRepository.save(any(AIConversation.class)))
                .thenReturn(conversation);

        when(conversation.getId()).thenReturn(100L);
        when(conversation.getMerchant()).thenReturn(merchant);
        when(conversation.getUser()).thenReturn(user);
        when(conversation.getTitle()).thenReturn("Payment analysis");
        when(conversation.getStatus())
                .thenReturn(AIConversationStatus.ACTIVE);

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        AIConversationResponse response =
                service.createConversation(
                        10L,
                        1L,
                        "Payment analysis"
                );

        assertEquals(100L, response.id());
        assertEquals("Payment analysis", response.title());
        assertEquals(AIConversationStatus.ACTIVE, response.status());

        verify(merchantRepository).findById(10L);

        verify(userRepository)
                .findByMerchantIdAndId(10L, 1L);

        verify(conversationRepository)
                .save(any(AIConversation.class));
    }

    @Test
    void shouldRejectConversationCreationWhenMerchantDoesNotExist() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.empty());

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createConversation(
                        10L,
                        1L,
                        "Payment analysis"
                )
        );

        verify(merchantRepository).findById(10L);

        verify(userRepository, never())
                .findByMerchantIdAndId(anyLong(), anyLong());

        verify(conversationRepository, never())
                .save(any(AIConversation.class));
    }

    @Test
    void shouldRejectConversationCreationWhenUserDoesNotExist() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        Merchant merchant = mock(Merchant.class);

        when(merchantRepository.findById(10L))
                .thenReturn(Optional.of(merchant));

        when(userRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.empty());

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createConversation(
                        10L,
                        1L,
                        "Payment analysis"
                )
        );

        verify(merchantRepository).findById(10L);

        verify(userRepository)
                .findByMerchantIdAndId(10L, 1L);

        verify(conversationRepository, never())
                .save(any(AIConversation.class));
    }

    @Test
    void shouldGetConversation() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);
        AIConversation conversation = mock(AIConversation.class);

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.of(conversation));

        when(conversation.getId()).thenReturn(100L);
        when(conversation.getMerchant()).thenReturn(merchant);
        when(conversation.getUser()).thenReturn(user);
        when(conversation.getTitle()).thenReturn("Payment analysis");
        when(conversation.getStatus())
                .thenReturn(AIConversationStatus.ACTIVE);

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        AIConversationResponse response =
                service.getConversation(10L, 100L);

        assertEquals(100L, response.id());
        assertEquals("Payment analysis", response.title());
        assertEquals(AIConversationStatus.ACTIVE, response.status());

        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);
    }

    @Test
    void shouldRejectGettingConversationWhenConversationDoesNotExist() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.empty());

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getConversation(10L, 100L)
        );

        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);
    }

    @Test
    void shouldGetConversations() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        AIConversation firstConversation = mock(AIConversation.class);
        AIConversation secondConversation = mock(AIConversation.class);

        Merchant firstMerchant = mock(Merchant.class);
        Merchant secondMerchant = mock(Merchant.class);

        User firstUser = mock(User.class);
        User secondUser = mock(User.class);

        when(conversationRepository.findByMerchantIdOrderByUpdatedAtDesc(10L))
                .thenReturn(List.of(firstConversation, secondConversation));

        when(firstConversation.getId()).thenReturn(100L);
        when(firstConversation.getMerchant()).thenReturn(firstMerchant);
        when(firstConversation.getUser()).thenReturn(firstUser);
        when(firstConversation.getTitle()).thenReturn("Payment analysis");
        when(firstConversation.getStatus())
                .thenReturn(AIConversationStatus.ACTIVE);

        when(secondConversation.getId()).thenReturn(101L);
        when(secondConversation.getMerchant()).thenReturn(secondMerchant);
        when(secondConversation.getUser()).thenReturn(secondUser);
        when(secondConversation.getTitle()).thenReturn("Settlement analysis");
        when(secondConversation.getStatus())
                .thenReturn(AIConversationStatus.ARCHIVED);

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        List<AIConversationResponse> responses =
                service.getConversations(10L);

        assertEquals(2, responses.size());

        assertEquals(100L, responses.get(0).id());
        assertEquals("Payment analysis", responses.get(0).title());
        assertEquals(AIConversationStatus.ACTIVE, responses.get(0).status());

        assertEquals(101L, responses.get(1).id());
        assertEquals("Settlement analysis", responses.get(1).title());
        assertEquals(AIConversationStatus.ARCHIVED, responses.get(1).status());

        verify(conversationRepository)
                .findByMerchantIdOrderByUpdatedAtDesc(10L);
    }

    @Test
    void shouldArchiveConversation() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        AIConversation conversation = mock(AIConversation.class);
        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.of(conversation));

        when(conversation.getId()).thenReturn(100L);
        when(conversation.getMerchant()).thenReturn(merchant);
        when(conversation.getUser()).thenReturn(user);
        when(conversation.getTitle()).thenReturn("Payment analysis");
        when(conversation.getStatus())
                .thenReturn(AIConversationStatus.ARCHIVED);

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        AIConversationResponse response =
                service.archiveConversation(10L, 100L);

        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);

        verify(conversation).archive();

        assertEquals(100L, response.id());
        assertEquals("Payment analysis", response.title());
        assertEquals(AIConversationStatus.ARCHIVED, response.status());
    }

    @Test
    void shouldRejectArchivingConversationWhenConversationDoesNotExist() {
        AIConversationRepository conversationRepository =
                mock(AIConversationRepository.class);
        MerchantRepository merchantRepository =
                mock(MerchantRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);

        when(conversationRepository.findByMerchantIdAndId(10L, 100L))
                .thenReturn(Optional.empty());

        AIConversationService service =
                new AIConversationServiceImpl(
                        conversationRepository,
                        merchantRepository,
                        userRepository
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.archiveConversation(10L, 100L)
        );

        verify(conversationRepository)
                .findByMerchantIdAndId(10L, 100L);
    }
}