package com.paypilot.backend.domain.reconciliation;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.settlement.Settlement;
import com.paypilot.backend.domain.reconciliation.dto.ReconciliationResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private SettlementReconciliationRepository reconciliationRepository;

    @InjectMocks
    private ReconciliationServiceImpl reconciliationService;

    @Test
    void shouldGetReconciliationForMerchant() {
        Long merchantId = 1L;
        Long settlementId = 10L;
        Long reconciliationId = 100L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        Settlement settlement = new Settlement(
                merchant,
                "setl_test_123",
                new BigDecimal("25000.00"),
                "INR"
        );

        SettlementReconciliation reconciliation =
                new SettlementReconciliation(
                        merchant,
                        settlement,
                        "pay_test_123",
                        ReconciliationTransactionType.PAYMENT,
                        2500000L,
                        "INR"
                );

        when(reconciliationRepository.findByMerchantIdAndId(
                merchantId,
                reconciliationId
        )).thenReturn(Optional.of(reconciliation));

        ReconciliationResponse response =
                reconciliationService.getReconciliation(
                        merchantId,
                        reconciliationId
                );

        assertThat(response).isNotNull();
        assertThat(response.merchantId()).isEqualTo(merchantId);
        assertThat(response.settlementId())
                .isEqualTo(settlement.getId());
        assertThat(response.razorpayEntityId())
                .isEqualTo("pay_test_123");
        assertThat(response.transactionType())
                .isEqualTo(ReconciliationTransactionType.PAYMENT);
        assertThat(response.amount())
                .isEqualTo(2500000L);
        assertThat(response.currency()).isEqualTo("INR");

        verify(reconciliationRepository)
                .findByMerchantIdAndId(
                        merchantId,
                        reconciliationId
                );
    }

    @Test
    void shouldThrowWhenReconciliationDoesNotExist() {
        Long merchantId = 1L;
        Long reconciliationId = 100L;

        when(reconciliationRepository.findByMerchantIdAndId(
                merchantId,
                reconciliationId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reconciliationService.getReconciliation(
                        merchantId,
                        reconciliationId
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reconciliation not found: " + reconciliationId);
    }

    @Test
    void shouldNotReturnReconciliationFromAnotherMerchant() {
        Long merchantId = 1L;
        Long reconciliationId = 100L;

        when(reconciliationRepository.findByMerchantIdAndId(
                merchantId,
                reconciliationId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reconciliationService.getReconciliation(
                        merchantId,
                        reconciliationId
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reconciliationRepository)
                .findByMerchantIdAndId(
                        merchantId,
                        reconciliationId
                );
    }

    @Test
    void shouldGetReconciliationsBySettlement() {
        Long merchantId = 1L;
        Long settlementId = 10L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        Settlement settlement = mock(Settlement.class);
        when(settlement.getId()).thenReturn(settlementId);

        SettlementReconciliation reconciliation =
                new SettlementReconciliation(
                        merchant,
                        settlement,
                        "pay_test_123",
                        ReconciliationTransactionType.PAYMENT,
                        2500000L,
                        "INR"
                );

        when(reconciliationRepository.findAllByMerchantIdAndSettlementId(
                merchantId,
                settlementId
        )).thenReturn(List.of(reconciliation));

        List<ReconciliationResponse> responses =
                reconciliationService.getReconciliationsBySettlement(
                        merchantId,
                        settlementId
                );

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).razorpayEntityId())
                .isEqualTo("pay_test_123");
        assertThat(responses.get(0).settlementId())
                .isEqualTo(settlementId);

        verify(reconciliationRepository)
                .findAllByMerchantIdAndSettlementId(
                        merchantId,
                        settlementId
                );
    }

    @Test
    void shouldGetReconciliationByRazorpayEntity() {
        Long merchantId = 1L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        Settlement settlement = mock(Settlement.class);
        when(settlement.getId()).thenReturn(10L);

        SettlementReconciliation reconciliation =
                new SettlementReconciliation(
                        merchant,
                        settlement,
                        "pay_test_456",
                        ReconciliationTransactionType.PAYMENT,
                        1500000L,
                        "INR"
                );

        when(reconciliationRepository.findByMerchantIdAndRazorpayEntityId(
                merchantId,
                "pay_test_456"
        )).thenReturn(Optional.of(reconciliation));

        ReconciliationResponse response =
                reconciliationService.getReconciliationByRazorpayEntity(
                        merchantId,
                        "pay_test_456"
                );

        assertThat(response).isNotNull();
        assertThat(response.razorpayEntityId())
                .isEqualTo("pay_test_456");
        assertThat(response.transactionType())
                .isEqualTo(ReconciliationTransactionType.PAYMENT);
        assertThat(response.amount())
                .isEqualTo(1500000L);

        verify(reconciliationRepository)
                .findByMerchantIdAndRazorpayEntityId(
                        merchantId,
                        "pay_test_456"
                );
    }

    @Test
    void shouldThrowWhenRazorpayEntityDoesNotExist() {
        Long merchantId = 1L;
        String razorpayEntityId = "pay_missing";

        when(reconciliationRepository.findByMerchantIdAndRazorpayEntityId(
                merchantId,
                razorpayEntityId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reconciliationService.getReconciliationByRazorpayEntity(
                        merchantId,
                        razorpayEntityId
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reconciliation not found: " + razorpayEntityId);
    }
}