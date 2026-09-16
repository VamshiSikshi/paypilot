package com.paypilot.backend.domain.settlement;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class SettlementRepositoryTest {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindSettlementByMerchantIdAndRazorpaySettlementId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Settlement settlement = settlementRepository.save(
                new Settlement(
                        merchant,
                        "setl_test_123",
                        new BigDecimal("25000.00"),
                        "INR"
                )
        );

        var result = settlementRepository.findByMerchantIdAndRazorpaySettlementId(
                merchant.getId(),
                "setl_test_123"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(settlement.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getRazorpaySettlementId())
                .isEqualTo("setl_test_123");
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("25000.00");
        assertThat(result.get().getCurrency())
                .isEqualTo("INR");
        assertThat(result.get().getStatus())
                .isEqualTo(SettlementStatus.CREATED);
        assertThat(result.get().getSettlementUtr())
                .isNull();
        assertThat(result.get().getFee())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getTax())
                .isEqualByComparingTo("0.00");
    }
}