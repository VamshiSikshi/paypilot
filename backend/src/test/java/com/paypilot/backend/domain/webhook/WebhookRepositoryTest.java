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
}