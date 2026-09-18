package com.paypilot.backend.domain.user;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindUserByMerchantIdAndEmail() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_123",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "owner@test.com",
                        "Test Owner",
                        UserRole.OWNER
                )
        );

        var result = userRepository.findByMerchantIdAndEmail(
                merchant.getId(),
                "owner@test.com"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo("owner@test.com");
        assertThat(result.get().getRole()).isEqualTo(UserRole.OWNER);
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.INVITED);
    }

    @Test
    void shouldFindUserByMerchantIdAndId() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_456",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "finance@test.com",
                        "Test Finance",
                        UserRole.FINANCE
                )
        );

        var result = userRepository.findByMerchantIdAndId(
                merchant.getId(),
                user.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
        assertThat(result.get().getEmail()).isEqualTo("finance@test.com");
    }

    @Test
    void shouldNotFindUserWhenMerchantIdDoesNotMatch() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_789",
                        "Test Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "viewer@test.com",
                        "Test Viewer",
                        UserRole.VIEWER
                )
        );

        var result = userRepository.findByMerchantIdAndId(
                merchant.getId() + 999,
                user.getId()
        );

        assertThat(result).isEmpty();
    }
}