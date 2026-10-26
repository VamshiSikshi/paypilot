package com.paypilot.backend.domain.razorpay;

public interface RazorpayTokenEncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext, String keyVersion);

    String getKeyVersion();
}
