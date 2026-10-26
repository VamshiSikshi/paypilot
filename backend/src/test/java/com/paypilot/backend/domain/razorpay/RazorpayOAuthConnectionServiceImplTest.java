package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.flyway.enabled=false")
class RazorpayOAuthConnectionServiceImplTest {

    @Autowired
    private RazorpayOAuthConnectionService oauthConnectionService;

    @Autowired
    private RazorpayTokenStorageService tokenStorageService;

    @Autowired
    private RazorpayOAuthTokenRepository tokenRepository;

    @Autowired
    private RazorpayConnectionRepository connectionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    void completeAuthorization_differentAccount_rollsBackTokenOverwrite() {
        Merchant merchant = merchantRepository.saveAndFlush(
                new Merchant(
                        "test-original-account",
                        "OAuth Rollback Test",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Long merchantId = merchant.getId();

        oauthConnectionService.completeAuthorization(
                merchantId,
                "test-original-account",
                "original-access-token",
                "original-refresh-token",
                Instant.now().plusSeconds(3600),
                "read_write"
        );

        RazorpayOAuthToken originalToken = tokenRepository
                .findByMerchantId(merchantId)
                .orElseThrow();

        String originalAccessCiphertext =
                originalToken.getAccessTokenCiphertext();
        String originalRefreshCiphertext =
                originalToken.getRefreshTokenCiphertext();
        String originalKeyVersion =
                originalToken.getEncryptionKeyVersion();

        assertThrows(
                IllegalStateException.class,
                () -> oauthConnectionService.completeAuthorization(
                        merchantId,
                        "test-different-account",
                        "replacement-access-token",
                        "replacement-refresh-token",
                        Instant.now().plusSeconds(3600),
                        "read_write"
                )
        );

        RazorpayOAuthToken tokenAfterFailure = tokenRepository
                .findByMerchantId(merchantId)
                .orElseThrow();

        assertEquals(
                originalAccessCiphertext,
                tokenAfterFailure.getAccessTokenCiphertext(),
                "Access token ciphertext must remain unchanged"
        );
        assertEquals(
                originalRefreshCiphertext,
                tokenAfterFailure.getRefreshTokenCiphertext(),
                "Refresh token ciphertext must remain unchanged"
        );
        assertEquals(
                originalKeyVersion,
                tokenAfterFailure.getEncryptionKeyVersion(),
                "Encryption key version must remain unchanged"
        );

        RazorpayConnection connectionAfterFailure = connectionRepository
                .findByMerchantId(merchantId)
                .orElseThrow();

        assertEquals(
                "test-original-account",
                connectionAfterFailure.getExternalAccountId(),
                "Existing Razorpay account must remain connected"
        );

        assertEquals(
                "original-access-token",
                tokenStorageService.retrieveAccessToken(
                        "razorpay-oauth-token:" + tokenAfterFailure.getId()
                )
        );
        assertEquals(
                "original-refresh-token",
                tokenStorageService.retrieveRefreshToken(
                        "razorpay-oauth-token:" + tokenAfterFailure.getId()
                )
        );
    }
}