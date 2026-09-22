package com.paypilot.backend.domain.webhook;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class WebhookRepositoryTest {

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindWebhookByMerchantIdAndEventId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Webhook webhook = webhookRepository.save(
                new Webhook(
                        merchant,
                        "evt_test_123",
                        "payment.captured",
                        "{\"id\":\"pay_test_123\"}",
                        "signature_test"
                )
        );

        var result = webhookRepository.findByMerchantIdAndEventId(
                merchant.getId(),
                "evt_test_123"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(webhook.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getEventId())
                .isEqualTo("evt_test_123");
        assertThat(result.get().getEventType())
                .isEqualTo("payment.captured");
        assertThat(result.get().getPayload())
                .isEqualTo("{\"id\":\"pay_test_123\"}");
        assertThat(result.get().getSignature())
                .isEqualTo("signature_test");
        assertThat(result.get().getStatus())
                .isEqualTo(WebhookStatus.RECEIVED);
        assertThat(result.get().getAttemptCount())
                .isZero();
        assertThat(result.get().getReceivedAt())
                .isNotNull();
        assertThat(result.get().getCreatedAt())
                .isNotNull();
        assertThat(result.get().getUpdatedAt())
                .isNotNull();
    }

    @Test
    void shouldFindWebhookByMerchantIdAndId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_456",
                        "Test Business 2",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Webhook webhook = webhookRepository.save(
                new Webhook(
                        merchant,
                        "evt_test_456",
                        "payment.failed",
                        "{\"id\":\"pay_test_456\"}",
                        "signature_test_456"
                )
        );

        var result = webhookRepository.findByMerchantIdAndId(
                merchant.getId(),
                webhook.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(webhook.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
    }

    @Test
    void shouldNotFindWebhookForAnotherMerchant() {
        Merchant merchant1 = merchantRepository.save(
                new Merchant(
                        "acc_test_789",
                        "Test Business 3",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant merchant2 = merchantRepository.save(
                new Merchant(
                        "acc_test_999",
                        "Test Business 4",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Webhook webhook = webhookRepository.save(
                new Webhook(
                        merchant1,
                        "evt_test_789",
                        "payment.captured",
                        "{\"id\":\"pay_test_789\"}",
                        "signature_test_789"
                )
        );

        var result = webhookRepository.findByMerchantIdAndId(
                merchant2.getId(),
                webhook.getId()
        );

        assertThat(result).isEmpty();
    }
}