package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class RazorpayConnectionRepositoryTest {

    @Autowired
    private RazorpayConnectionRepository connectionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindConnectionByMerchantId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_connection_123",
                        "Connection Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        RazorpayConnection saved = connectionRepository.save(
                new RazorpayConnection(merchant, "acc_external_123")
        );

        var result = connectionRepository.findByMerchantId(merchant.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
        assertThat(result.get().getExternalAccountId())
                .isEqualTo("acc_external_123");
        assertThat(result.get().getStatus())
                .isEqualTo(RazorpayConnectionStatus.CONNECTED);
    }

    @Test
    void shouldFindConnectionByExternalAccountId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_connection_456",
                        "External Account Test",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        RazorpayConnection saved = connectionRepository.save(
                new RazorpayConnection(merchant, "acc_external_456")
        );

        var result = connectionRepository.findByExternalAccountId(
                "acc_external_456"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getMerchant().getId())
                .isEqualTo(merchant.getId());
    }

    @Test
    void shouldNotFindConnectionForUnknownMerchant() {
        assertThat(connectionRepository.findByMerchantId(-999L)).isEmpty();
    }

    @Test
    void shouldNotFindConnectionForUnknownExternalAccount() {
        assertThat(
                connectionRepository.findByExternalAccountId("acc_missing")
        ).isEmpty();
    }


    @Test
    @Transactional
    void shouldRejectSecondConnectionForSameMerchant() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_duplicate_merchant",
                        "Duplicate Merchant Test",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        connectionRepository.saveAndFlush(
                new RazorpayConnection(merchant, "acc_external_first")
        );

        assertThatThrownBy(() ->
                connectionRepository.saveAndFlush(
                        new RazorpayConnection(merchant, "acc_external_second")
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldRejectDuplicateExternalAccountAcrossMerchants() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "acc_external_owner_1",
                        "First Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "acc_external_owner_2",
                        "Second Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        connectionRepository.saveAndFlush(
                new RazorpayConnection(firstMerchant, "acc_external_shared")
        );

        assertThatThrownBy(() ->
                connectionRepository.saveAndFlush(
                        new RazorpayConnection(
                                secondMerchant,
                                "acc_external_shared"
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}