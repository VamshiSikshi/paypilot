package com.paypilot.backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthAccountRepository
        extends JpaRepository<UserAuthAccount, Long> {

    Optional<UserAuthAccount> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    Optional<UserAuthAccount> findByUserMerchantIdAndUserEmail(
            Long merchantId,
            String email
    );

    Optional<UserAuthAccount> findByUserMerchantRazorpayAccountIdAndUserEmail(
            String razorpayAccountId,
            String email
    );

    Optional<UserAuthAccount> findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
            String razorpayAccountId,
            AuthProvider provider,
            String providerUserId
    );
}

