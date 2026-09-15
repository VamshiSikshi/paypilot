package com.paypilot.backend.domain.merchant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class MerchantRepositoryTest {

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindMerchantByRazorpayAccountId() {
        Merchant merchant = new Merchant(
                "acc_test_123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        Merchant savedMerchant = merchantRepository.save(merchant);

        var result = merchantRepository
                .findByRazorpayAccountId("acc_test_123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedMerchant.getId());
        assertThat(result.get().getBusinessName())
                .isEqualTo("Test Business");
        assertThat(result.get().getStatus())
                .isEqualTo(MerchantStatus.ACTIVE);
    }
}