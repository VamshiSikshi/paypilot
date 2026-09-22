package com.paypilot.backend.domain.settlement;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.settlement.dto.SettlementResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Test
    void shouldGetSettlementForMerchant() {
        Long merchantId = 1L;
        Long settlementId = 10L;

        Merchant merchant = mock(Merchant.class);
        when(merchant.getId()).thenReturn(merchantId);

        Settlement settlement = new Settlement(
                merchant,
                "setl_test_123",
                new BigDecimal("25000.00"),
                "INR"
        );

        when(settlementRepository.findByMerchantIdAndId(
                merchantId,
                settlementId
        )).thenReturn(Optional.of(settlement));

        SettlementResponse response =
                settlementService.getSettlement(merchantId, settlementId);

        assertThat(response).isNotNull();
        assertThat(response.merchantId()).isEqualTo(merchantId);
        assertThat(response.razorpaySettlementId())
                .isEqualTo("setl_test_123");
        assertThat(response.amount())
                .isEqualByComparingTo("25000.00");
        assertThat(response.currency()).isEqualTo("INR");
        assertThat(response.status())
                .isEqualTo(SettlementStatus.CREATED);

        verify(settlementRepository)
                .findByMerchantIdAndId(merchantId, settlementId);
    }

    @Test
    void shouldThrowWhenSettlementDoesNotExist() {
        Long merchantId = 1L;
        Long settlementId = 10L;

        when(settlementRepository.findByMerchantIdAndId(
                merchantId,
                settlementId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                settlementService.getSettlement(merchantId, settlementId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Settlement not found: " + settlementId);
    }

    @Test
    void shouldNotReturnSettlementFromAnotherMerchant() {
        Long merchantId = 1L;
        Long settlementId = 10L;

        when(settlementRepository.findByMerchantIdAndId(
                merchantId,
                settlementId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                settlementService.getSettlement(merchantId, settlementId)
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(settlementRepository)
                .findByMerchantIdAndId(merchantId, settlementId);
    }
}