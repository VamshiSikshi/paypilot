package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RazorpayOAuthStateServiceImpl
        implements RazorpayOAuthStateService {

    private static final int STATE_BYTES = 32;
    private static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MerchantRepository merchantRepository;
    private final RazorpayOAuthStateRepository stateRepository;

    public RazorpayOAuthStateServiceImpl(
            MerchantRepository merchantRepository,
            RazorpayOAuthStateRepository stateRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    @Transactional
    public String createState(Long merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID is required");
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        byte[] randomBytes = new byte[STATE_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);

        String rawState = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String stateHash = sha256Hex(rawState);
        Instant expiresAt = Instant.now().plus(STATE_TTL);

        RazorpayOAuthState state = new RazorpayOAuthState(
                merchant,
                stateHash,
                expiresAt
        );

        stateRepository.save(state);
        return rawState;
    }

    @Override
    @Transactional
    public boolean consumeState(String rawState, Long merchantId) {
        if (rawState == null || rawState.isBlank() || merchantId == null) {
            return false;
        }

        String stateHash = sha256Hex(rawState);

        return stateRepository.consumeIfValid(stateHash, merchantId) == 1;
    }

    @Override
    @Transactional
    public Optional<Long> consumeStateAndGetMerchantId(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            return Optional.empty();
        }

        String stateHash = sha256Hex(rawState);

        Optional<Long> merchantId =
                stateRepository.findMerchantIdByStateHash(stateHash);

        if (merchantId.isEmpty()) {
            return Optional.empty();
        }

        int updatedRows = stateRepository.consumeIfValid(
                stateHash,
                merchantId.get()
        );

        if (updatedRows != 1) {
            return Optional.empty();
        }

        return merchantId;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }
}