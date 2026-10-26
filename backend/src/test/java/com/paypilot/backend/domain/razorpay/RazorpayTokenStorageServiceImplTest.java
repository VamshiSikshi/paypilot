package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayTokenStorageServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private RazorpayOAuthTokenRepository tokenRepository;

    @Mock
    private RazorpayTokenEncryptionService encryptionService;

    @Mock
    private Merchant merchant;

    @Mock
    private RazorpayOAuthToken savedToken;

    private RazorpayTokenStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RazorpayTokenStorageServiceImpl(
                merchantRepository,
                tokenRepository,
                encryptionService
        );
    }

    @Test
    void shouldEncryptAndSaveTokens() {
        Long merchantId = 10L;

        when(merchantRepository.findById(merchantId))
                .thenReturn(Optional.of(merchant));
        when(tokenRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.empty());
        when(encryptionService.encrypt("access-secret"))
                .thenReturn("encrypted-access");
        when(encryptionService.encrypt("refresh-secret"))
                .thenReturn("encrypted-refresh");
        when(encryptionService.getKeyVersion()).thenReturn("v1");
        when(tokenRepository.saveAndFlush(any(RazorpayOAuthToken.class)))
                .thenReturn(savedToken);
        when(savedToken.getId()).thenReturn(42L);

        String reference = service.storeTokens(
                merchantId, "access-secret", "refresh-secret"
        );

        assertEquals("razorpay-oauth-token:42", reference);

        verify(encryptionService).encrypt("access-secret");
        verify(encryptionService).encrypt("refresh-secret");
        verify(tokenRepository).saveAndFlush(argThat(token ->
                "encrypted-access".equals(token.getAccessTokenCiphertext())
                        && "encrypted-refresh".equals(
                                token.getRefreshTokenCiphertext())
                        && "v1".equals(token.getEncryptionKeyVersion())
        ));
    }

    @Test
    void shouldAllowMissingRefreshToken() {
        Long merchantId = 10L;

        when(merchantRepository.findById(merchantId))
                .thenReturn(Optional.of(merchant));
        when(tokenRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.empty());
        when(encryptionService.encrypt("access-secret"))
                .thenReturn("encrypted-access");
        when(encryptionService.getKeyVersion()).thenReturn("v1");
        when(tokenRepository.saveAndFlush(any(RazorpayOAuthToken.class)))
                .thenReturn(savedToken);
        when(savedToken.getId()).thenReturn(42L);

        assertEquals(
                "razorpay-oauth-token:42",
                service.storeTokens(merchantId, "access-secret", null)
        );

        verify(encryptionService, never()).encrypt("refresh-secret");
        verify(tokenRepository).saveAndFlush(argThat(token ->
                token.getRefreshTokenCiphertext() == null
        ));
    }

    @Test
    void shouldReplaceExistingEncryptedTokens() {
        Long merchantId = 10L;
        RazorpayOAuthToken existing = mock(RazorpayOAuthToken.class);

        when(merchantRepository.findById(merchantId))
                .thenReturn(Optional.of(merchant));
        when(tokenRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(existing));
        when(encryptionService.encrypt("new-access"))
                .thenReturn("new-encrypted-access");
        when(encryptionService.encrypt("new-refresh"))
                .thenReturn("new-encrypted-refresh");
        when(encryptionService.getKeyVersion()).thenReturn("v1");
        when(tokenRepository.saveAndFlush(existing)).thenReturn(existing);
        when(existing.getId()).thenReturn(42L);

        String reference = service.storeTokens(
                merchantId, "new-access", "new-refresh"
        );

        assertEquals("razorpay-oauth-token:42", reference);
        verify(existing).updateEncryptedTokens(
                "new-encrypted-access",
                "new-encrypted-refresh",
                "v1"
        );
        verify(tokenRepository).saveAndFlush(existing);
    }

    @Test
    void shouldDecryptAccessTokenOnRetrieval() {
        when(tokenRepository.findById(42L))
                .thenReturn(Optional.of(savedToken));
        when(savedToken.getAccessTokenCiphertext())
                .thenReturn("encrypted-access");
        when(savedToken.getEncryptionKeyVersion()).thenReturn("v1");
        when(encryptionService.decrypt("encrypted-access", "v1"))
                .thenReturn("access-secret");

        assertEquals(
                "access-secret",
                service.retrieveAccessToken("razorpay-oauth-token:42")
        );
    }

    @Test
    void shouldDecryptRefreshTokenOnRetrieval() {
        when(tokenRepository.findById(42L))
                .thenReturn(Optional.of(savedToken));
        when(savedToken.getRefreshTokenCiphertext())
                .thenReturn("encrypted-refresh");
        when(savedToken.getEncryptionKeyVersion()).thenReturn("v1");
        when(encryptionService.decrypt("encrypted-refresh", "v1"))
                .thenReturn("refresh-secret");

        assertEquals(
                "refresh-secret",
                service.retrieveRefreshToken("razorpay-oauth-token:42")
        );
    }

    @Test
    void shouldRejectMissingMerchant() {
        when(merchantRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.storeTokens(10L, "access-secret", null)
        );

        verifyNoInteractions(tokenRepository, encryptionService);
    }

    @Test
    void shouldRejectBlankAccessToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.storeTokens(10L, " ", null)
        );

        verifyNoInteractions(merchantRepository, tokenRepository);
    }

    @Test
    void shouldRejectInvalidTokenReference() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.retrieveAccessToken("invalid-reference")
        );

        verifyNoInteractions(tokenRepository, encryptionService);
    }
}
