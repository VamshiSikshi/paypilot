package com.paypilot.backend.domain.ai;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIInsightServiceImplTest {

    @Mock
    private AIInsightRepository insightRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private AIInsightServiceImpl insightService;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );
    }

    @Test
    void shouldCreateInsight() {
        Instant validFrom = Instant.now();

        AIInsight insight = new AIInsight(
                merchant,
                "PAYMENT_FAILURE_SPIKE",
                InsightSeverity.HIGH,
                "Payment failures increased",
                "Payment failures have increased significantly.",
                InsightSource.AI,
                "{\"failureRate\":25.5}",
                validFrom,
                null
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(insightRepository.save(any(AIInsight.class)))
                .thenReturn(insight);

        var result = insightService.createInsight(
                1L,
                "PAYMENT_FAILURE_SPIKE",
                InsightSeverity.HIGH,
                "Payment failures increased",
                "Payment failures have increased significantly.",
                InsightSource.AI,
                "{\"failureRate\":25.5}",
                validFrom,
                null
        );

        assertThat(result.merchantId()).isEqualTo(merchant.getId());
        assertThat(result.insightType()).isEqualTo("PAYMENT_FAILURE_SPIKE");
        assertThat(result.severity()).isEqualTo(InsightSeverity.HIGH);
        assertThat(result.title()).isEqualTo("Payment failures increased");
        assertThat(result.description())
                .isEqualTo("Payment failures have increased significantly.");
        assertThat(result.status()).isEqualTo(InsightStatus.ACTIVE);
        assertThat(result.source()).isEqualTo(InsightSource.AI);
        assertThat(result.data()).isEqualTo("{\"failureRate\":25.5}");

        verify(merchantRepository).findById(1L);
        verify(insightRepository).save(any(AIInsight.class));
    }

    @Test
    void shouldRejectInsightCreationWhenMerchantDoesNotExist() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                insightService.createInsight(
                        1L,
                        "PAYMENT_FAILURE_SPIKE",
                        InsightSeverity.HIGH,
                        "Payment failures increased",
                        "Payment failures have increased significantly.",
                        InsightSource.AI,
                        "{\"failureRate\":25.5}",
                        Instant.now(),
                        null
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Merchant not found");

        verify(insightRepository, never()).save(any());
    }

    @Test
    void shouldGetInsight() {
        AIInsight insight = new AIInsight(
                merchant,
                "PAYMENT_FAILURE_SPIKE",
                InsightSeverity.HIGH,
                "Payment failures increased",
                "Payment failures have increased significantly.",
                InsightSource.AI,
                "{\"failureRate\":25.5}",
                Instant.now(),
                null
        );

        when(insightRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.of(insight));

        var result = insightService.getInsight(1L, 10L);

        assertThat(result.insightType())
                .isEqualTo("PAYMENT_FAILURE_SPIKE");
        assertThat(result.severity())
                .isEqualTo(InsightSeverity.HIGH);
        assertThat(result.status())
                .isEqualTo(InsightStatus.ACTIVE);

        verify(insightRepository)
                .findByMerchantIdAndId(1L, 10L);
    }

    @Test
    void shouldRejectGettingInsightWhenInsightDoesNotExist() {
        when(insightRepository.findByMerchantIdAndId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                insightService.getInsight(1L, 10L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("AI insight not found");
    }

    @Test
    void shouldGetInsights() {
        AIInsight firstInsight = new AIInsight(
                merchant,
                "PAYMENT_FAILURE_SPIKE",
                InsightSeverity.HIGH,
                "Payment failures increased",
                "Payment failures have increased significantly.",
                InsightSource.AI,
                "{\"failureRate\":25.5}",
                Instant.parse("2026-09-25T10:00:00Z"),
                null
        );

        AIInsight secondInsight = new AIInsight(
                merchant,
                "SETTLEMENT_DELAY",
                InsightSeverity.MEDIUM,
                "Settlement delayed",
                "Settlement processing is taking longer than usual.",
                InsightSource.ANALYTICS,
                "{\"delayHours\":12}",
                Instant.parse("2026-09-24T10:00:00Z"),
                null
        );

        when(insightRepository.findByMerchantIdOrderByValidFromDesc(1L))
                .thenReturn(List.of(firstInsight, secondInsight));

        var result = insightService.getInsights(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).insightType())
                .isEqualTo("PAYMENT_FAILURE_SPIKE");
        assertThat(result.get(1).insightType())
                .isEqualTo("SETTLEMENT_DELAY");

        verify(insightRepository)
                .findByMerchantIdOrderByValidFromDesc(1L);
    }
}