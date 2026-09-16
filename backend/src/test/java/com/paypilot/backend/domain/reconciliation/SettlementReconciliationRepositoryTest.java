package com.paypilot.backend.domain.reconciliation;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.settlement.Settlement;
import com.paypilot.backend.domain.settlement.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class SettlementReconciliationRepositoryTest {

    @Autowired
    private SettlementReconciliationRepository reconciliationRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindReconciliationByMerchantIdAndRazorpayEntityId() {
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

        SettlementReconciliation reconciliation =
                reconciliationRepository.save(
                        new SettlementReconciliation(
                                merchant,
                                settlement,
                                "pay_test_123",
                                ReconciliationTransactionType.PAYMENT,
                                new BigDecimal("1500.00"),
                                "INR"
                        )
                );

        var result =
                reconciliationRepository.findByMerchantIdAndRazorpayEntityId(
                        merchant.getId(),
                        "pay_test_123"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(reconciliation.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getSettlement().getId())
                .isEqualTo(settlement.getId());
        assertThat(result.get().getRazorpayEntityId())
                .isEqualTo("pay_test_123");
        assertThat(result.get().getTransactionType())
                .isEqualTo(ReconciliationTransactionType.PAYMENT);
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("1500.00");
        assertThat(result.get().getDebit())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getCredit())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getFee())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getTax())
                .isEqualByComparingTo("0.00");
        assertThat(result.get().getOnHold())
                .isFalse();
        assertThat(result.get().getSettled())
                .isFalse();
    }
}