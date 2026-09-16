package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class AIInsightRepositoryTest {

    @Autowired
    private AIInsightRepository insightRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindInsightByMerchantIdAndId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Instant validFrom = Instant.now();

        AIInsight insight = insightRepository.save(
                new AIInsight(
                        merchant,
                        "PAYMENT_FAILURE_SPIKE",
                        InsightSeverity.HIGH,
                        "Payment failures increased",
                        "Payment failures have increased significantly.",
                        InsightSource.AI,
                        "{\"failureRate\":25.5}",
                        validFrom,
                        null
                )
        );

        var result = insightRepository.findByMerchantIdAndId(
                merchant.getId(),
                insight.getId()
        );

        assertThat(result).isPresent();

        AIInsight savedInsight = result.get();

        assertThat(savedInsight.getId())
                .isEqualTo(insight.getId());
        assertThat(savedInsight.getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(savedInsight.getInsightType())
                .isEqualTo("PAYMENT_FAILURE_SPIKE");
        assertThat(savedInsight.getSeverity())
                .isEqualTo(InsightSeverity.HIGH);
        assertThat(savedInsight.getTitle())
                .isEqualTo("Payment failures increased");
        assertThat(savedInsight.getDescription())
                .isEqualTo("Payment failures have increased significantly.");
        assertThat(savedInsight.getStatus())
                .isEqualTo(InsightStatus.ACTIVE);
        assertThat(savedInsight.getSource())
                .isEqualTo(InsightSource.AI);
        assertThat(savedInsight.getData())
                .isEqualTo("{\"failureRate\":25.5}");
        assertThat(savedInsight.getValidFrom())
                .isEqualTo(validFrom);
        assertThat(savedInsight.getValidUntil())
                .isNull();
        assertThat(savedInsight.getCreatedAt())
                .isNotNull();
        assertThat(savedInsight.getUpdatedAt())
                .isNotNull();
    }
}