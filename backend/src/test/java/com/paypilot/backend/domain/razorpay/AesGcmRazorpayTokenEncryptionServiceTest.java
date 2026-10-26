package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.config.RazorpayTokenEncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmRazorpayTokenEncryptionServiceTest {

    private static final String TEST_KEY =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private AesGcmRazorpayTokenEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new AesGcmRazorpayTokenEncryptionService(
                new RazorpayTokenEncryptionProperties(TEST_KEY, "v1")
        );
    }

    @Test
    void shouldEncryptAndDecryptToken() {
        String token = "test-access-token-value";

        String ciphertext = service.encrypt(token);

        assertNotEquals(token, ciphertext);
        assertEquals(token, service.decrypt(ciphertext, "v1"));
    }

    @Test
    void shouldUseDifferentNonceForEachEncryption() {
        String token = "same-token";

        String first = service.encrypt(token);
        String second = service.encrypt(token);

        assertNotEquals(first, second);
        assertEquals(token, service.decrypt(first, "v1"));
        assertEquals(token, service.decrypt(second, "v1"));
    }

    @Test
    void shouldRejectBlankPlaintext() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.encrypt(" ")
        );
    }

    @Test
    void shouldRejectBlankCiphertext() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decrypt(" ", "v1")
        );
    }

    @Test
    void shouldRejectWrongKeyVersion() {
        String ciphertext = service.encrypt("test-token");

        assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(ciphertext, "v2")
        );
    }

    @Test
    void shouldRejectTamperedCiphertext() {
        byte[] packed = Base64.getDecoder().decode(
                service.encrypt("test-token")
        );

        packed[packed.length - 1] ^= 1;
        String tampered = Base64.getEncoder().encodeToString(packed);

        assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(tampered, "v1")
        );
    }


    @Test
    void shouldRejectMissingEncryptionKey() {
        assertThrows(
                IllegalStateException.class,
                () -> new AesGcmRazorpayTokenEncryptionService(
                        new RazorpayTokenEncryptionProperties("", "v1")
                )
        );
    }

    @Test
    void shouldRejectInvalidEncryptionKeyLength() {
        String invalidKey = Base64.getEncoder().encodeToString(
                new byte[16]
        );

        assertThrows(
                IllegalStateException.class,
                () -> new AesGcmRazorpayTokenEncryptionService(
                        new RazorpayTokenEncryptionProperties(
                                invalidKey, "v1"
                        )
                )
        );
    }
}