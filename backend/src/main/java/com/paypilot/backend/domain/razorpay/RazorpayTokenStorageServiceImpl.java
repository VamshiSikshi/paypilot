package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class RazorpayTokenStorageServiceImpl
        implements RazorpayTokenStorageService {

    private static final String REFERENCE_PREFIX = "razorpay-oauth-token:";

    private final MerchantRepository merchantRepository;
    private final RazorpayOAuthTokenRepository tokenRepository;
    private final RazorpayTokenEncryptionService encryptionService;

    public RazorpayTokenStorageServiceImpl(
            MerchantRepository merchantRepository,
            RazorpayOAuthTokenRepository tokenRepository,
            RazorpayTokenEncryptionService encryptionService
    ) {
        this.merchantRepository = Objects.requireNonNull(merchantRepository);
        this.tokenRepository = Objects.requireNonNull(tokenRepository);
        this.encryptionService = Objects.requireNonNull(encryptionService);
    }

    @Override
    @Transactional
    public String storeTokens(
            Long merchantId,
            String accessToken,
            String refreshToken
    ) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID is required");
        }
        if (!StringUtils.hasText(accessToken)) {
            throw new IllegalArgumentException("Access token is required");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Merchant not found"));

        String encryptedAccessToken = encryptionService.encrypt(accessToken);
        String encryptedRefreshToken = StringUtils.hasText(refreshToken)
                ? encryptionService.encrypt(refreshToken)
                : null;
        String keyVersion = encryptionService.getKeyVersion();

        RazorpayOAuthToken tokenRecord = tokenRepository
                .findByMerchantId(merchantId)
                .orElse(null);

        if (tokenRecord == null) {
            tokenRecord = new RazorpayOAuthToken(
                    merchant,
                    encryptedAccessToken,
                    encryptedRefreshToken,
                    keyVersion
            );
        } else {
            tokenRecord.updateEncryptedTokens(
                    encryptedAccessToken,
                    encryptedRefreshToken,
                    keyVersion
            );
        }

        RazorpayOAuthToken saved = tokenRepository.saveAndFlush(tokenRecord);
        return REFERENCE_PREFIX + saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public String retrieveAccessToken(String tokenReference) {
        RazorpayOAuthToken tokenRecord = findByReference(tokenReference);
        return encryptionService.decrypt(
                tokenRecord.getAccessTokenCiphertext(),
                tokenRecord.getEncryptionKeyVersion()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String retrieveRefreshToken(String tokenReference) {
        RazorpayOAuthToken tokenRecord = findByReference(tokenReference);

        if (!StringUtils.hasText(tokenRecord.getRefreshTokenCiphertext())) {
            throw new IllegalStateException(
                    "No refresh token is stored for this connection"
            );
        }

        return encryptionService.decrypt(
                tokenRecord.getRefreshTokenCiphertext(),
                tokenRecord.getEncryptionKeyVersion()
        );
    }

    private RazorpayOAuthToken findByReference(String tokenReference) {
        if (!StringUtils.hasText(tokenReference)
                || !tokenReference.startsWith(REFERENCE_PREFIX)) {
            throw new IllegalArgumentException("Invalid token reference");
        }

        String idPart = tokenReference.substring(REFERENCE_PREFIX.length());

        final Long tokenId;
        try {
            tokenId = Long.valueOf(idPart);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid token reference");
        }

        if (tokenId <= 0) {
            throw new IllegalArgumentException("Invalid token reference");
        }

        return tokenRepository.findById(tokenId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Token record not found"));
    }
}
