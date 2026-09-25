package com.paypilot.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void shouldCreatePasswordEncoder() {
        PasswordEncoder passwordEncoder =
                new PasswordConfig().passwordEncoder();

        String rawPassword = "TestPassword123!";
        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword
                )
        );
        assertFalse(
                passwordEncoder.matches(
                        "WrongPassword123!",
                        encodedPassword
                )
        );
    }
}