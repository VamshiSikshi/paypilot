package com.paypilot.backend.security;

import com.paypilot.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "paypilot-development-secret-key-must-be-at-least-32-bytes";

    private final JwtService jwtService = new JwtService(
            new JwtProperties(
                    SECRET,
                    86_400_000L,
                    "paypilot"
            )
    );

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtService.generateToken(
                1L,
                10L,
                "OWNER"
        );

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("merchantId", Long.class)).isEqualTo(10L);
        assertThat(claims.get("role", String.class)).isEqualTo("OWNER");
        assertThat(claims.getIssuer()).isEqualTo("paypilot");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void shouldRejectInvalidToken() {
        String token = jwtService.generateToken(
                1L,
                10L,
                "OWNER"
        );

        String tamperedToken = token.substring(0, token.length() - 1) + "x";

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> jwtService.parseToken(tamperedToken)
        );
    }
}