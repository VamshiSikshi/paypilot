package com.paypilot.backend.domain.user;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class UserAuthAccountRepositoryTest {

    @Autowired
    private UserAuthAccountRepository userAuthAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void shouldFindGoogleAccountByProviderAndProviderUserId() {
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

        UserAuthAccount account = userAuthAccountRepository.save(
                UserAuthAccount.google(user, "google-user-123")
        );

        var result = userAuthAccountRepository
                .findByProviderAndProviderUserId(
                        AuthProvider.GOOGLE,
                        "google-user-123"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(account.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.get().getProviderUserId())
                .isEqualTo("google-user-123");
        assertThat(result.get().getPasswordHash()).isNull();
    }

    @Test
    void shouldFindLocalAccountByMerchantIdAndEmail() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_test_local_123",
                        "Local Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "local@test.com",
                        "Local User",
                        UserRole.OWNER
                )
        );

        UserAuthAccount account = userAuthAccountRepository.save(
                UserAuthAccount.local(user, "hashed-password")
        );

        var result = userAuthAccountRepository
                .findByUserMerchantIdAndUserEmail(
                        merchant.getId(),
                        "local@test.com"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(account.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(result.get().getPasswordHash())
                .isEqualTo("hashed-password");
        assertThat(result.get().getProviderUserId()).isNull();
    }

    @Test
    void shouldNotFindLocalAccountWhenMerchantIdDoesNotMatch() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "acc_test_first_123",
                        "First Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "acc_test_second_123",
                        "Second Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        firstMerchant,
                        "same@test.com",
                        "First User",
                        UserRole.OWNER
                )
        );

        userAuthAccountRepository.save(
                UserAuthAccount.local(user, "hashed-password")
        );

        var result = userAuthAccountRepository
                .findByUserMerchantIdAndUserEmail(
                        secondMerchant.getId(),
                        "same@test.com"
                );

        assertThat(result).isEmpty();
    }
}