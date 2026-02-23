package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final SecretKey secretKey;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtTokenService(
            @Value("${app.auth.jwt-secret}") String jwtSecret,
            @Value("${app.auth.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.auth.refresh-token-days}") long refreshTokenDays
    ) {
        this.secretKey = deriveKey(jwtSecret);
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String generateAccessToken(Long userId, String username, ActorRole role, Long ownerId, boolean mustChangePassword) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenMinutes * 60);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role.name())
                .claim("ownerId", ownerId)
                .claim("mustChangePassword", mustChangePassword)
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenDays * 24 * 60 * 60);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public LocalDateTime getRefreshTokenExpiryTime() {
        return LocalDateTime.now().plusDays(refreshTokenDays);
    }

    public long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    private SecretKey deriveKey(String secret) {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            bytes = String.format("%-32s", secret).replace(' ', '0').getBytes(StandardCharsets.UTF_8);
        }
        Key key = Keys.hmacShaKeyFor(bytes);
        return (SecretKey) key;
    }
}
