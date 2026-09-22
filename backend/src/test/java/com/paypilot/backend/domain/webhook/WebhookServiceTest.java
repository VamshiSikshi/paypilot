package com.paypilot.backend.domain.webhook;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.webhook.dto.WebhookResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookRepository webhookRepository;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    @Test
    void shouldGetWebhookForMerchant() {
        Long merchantId = 1L;
        Long webhookId = 10L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        Webhook webhook = new Webhook(
                merchant,
                "evt_test_123",
                "payment.captured",
                "{\"id\":\"pay_test_123\"}",
                "signature_test"
        );

        when(webhookRepository.findByMerchantIdAndId(
                merchantId,
                webhookId
        )).thenReturn(Optional.of(webhook));

        WebhookResponse response =
                webhookService.getWebhook(merchantId, webhookId);

        assertThat(response).isNotNull();
        assertThat(response.merchantId()).isEqualTo(merchantId);
        assertThat(response.eventId()).isEqualTo("evt_test_123");
        assertThat(response.eventType()).isEqualTo("payment.captured");
        assertThat(response.payload())
                .isEqualTo("{\"id\":\"pay_test_123\"}");
        assertThat(response.signature())
                .isEqualTo("signature_test");
        assertThat(response.status())
                .isEqualTo(WebhookStatus.RECEIVED);
        assertThat(response.attemptCount()).isZero();

        verify(webhookRepository)
                .findByMerchantIdAndId(merchantId, webhookId);
    }

    @Test
    void shouldThrowWhenWebhookDoesNotExist() {
        Long merchantId = 1L;
        Long webhookId = 10L;

        when(webhookRepository.findByMerchantIdAndId(
                merchantId,
                webhookId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                webhookService.getWebhook(merchantId, webhookId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook not found: " + webhookId);
    }

    @Test
    void shouldNotReturnWebhookFromAnotherMerchant() {
        Long merchantId = 2L;
        Long webhookId = 10L;

        when(webhookRepository.findByMerchantIdAndId(
                merchantId,
                webhookId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                webhookService.getWebhook(merchantId, webhookId)
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(webhookRepository)
                .findByMerchantIdAndId(merchantId, webhookId);
    }
}