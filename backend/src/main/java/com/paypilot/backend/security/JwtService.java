package com.paypilot.backend.security;

import com.paypilot.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public long getExpiration() {
        return properties.expiration();
    }

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(Long userId, Long merchantId, String role) {
        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + properties.expiration()
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("merchantId", merchantId)
                .claim("role", role)
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}