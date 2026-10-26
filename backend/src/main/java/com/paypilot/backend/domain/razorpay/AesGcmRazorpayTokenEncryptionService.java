package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayTokenEncryptionProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Service
public class AesGcmRazorpayTokenEncryptionService
        implements RazorpayTokenEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final RazorpayTokenEncryptionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmRazorpayTokenEncryptionService(
            RazorpayTokenEncryptionProperties properties
    ) {
        this.properties = Objects.requireNonNull(properties);

        getKeyVersion();
        getSecretKey();
    }

    @Override
    public String encrypt(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            throw new IllegalArgumentException(
                    "Plaintext token must not be blank"
            );
        }

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    getSecretKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );

            byte[] encrypted = cipher.doFinal(
                    plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            ByteBuffer output = ByteBuffer.allocate(
                    nonce.length + encrypted.length
            );
            output.put(nonce);
            output.put(encrypted);

            return Base64.getEncoder().encodeToString(output.array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to encrypt Razorpay token", exception
            );
        }
    }

    @Override
    public String decrypt(String ciphertext, String keyVersion) {
        if (!StringUtils.hasText(ciphertext)) {
            throw new IllegalArgumentException(
                    "Ciphertext must not be blank"
            );
        }

        if (!getKeyVersion().equals(keyVersion)) {
            throw new IllegalStateException(
                    "No encryption key is configured for the stored key version"
            );
        }

        byte[] packed;
        try {
            packed = Base64.getDecoder().decode(ciphertext);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Ciphertext is not valid Base64", exception
            );
        }

        if (packed.length <= NONCE_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Ciphertext is too short"
            );
        }

        byte[] nonce = Arrays.copyOfRange(
                packed, 0, NONCE_LENGTH_BYTES
        );
        byte[] encrypted = Arrays.copyOfRange(
                packed, NONCE_LENGTH_BYTES, packed.length
        );

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getSecretKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(
                    plaintext,
                    java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to decrypt Razorpay token; ciphertext may be invalid "
                            + "or authentication may have failed",
                    exception
            );
        }
    }

    @Override
    public String getKeyVersion() {
        String version = properties.keyVersion();
        if (!StringUtils.hasText(version)) {
            throw new IllegalStateException(
                    "Razorpay token encryption key version is not configured"
            );
        }
        return version;
    }

    private SecretKeySpec getSecretKey() {
        String encodedKey = properties.keyBase64();

        if (!StringUtils.hasText(encodedKey)) {
            throw new IllegalStateException(
                    "Razorpay token encryption key is not configured"
            );
        }

        final byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Razorpay token encryption key must be valid Base64",
                    exception
            );
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            Arrays.fill(keyBytes, (byte) 0);
            throw new IllegalStateException(
                    "Razorpay token encryption key must decode to exactly 32 bytes"
            );
        }

        try {
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
}