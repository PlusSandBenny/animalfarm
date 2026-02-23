package com.animalfarm.auth;

import com.animalfarm.exception.UnauthorizedException;
import com.animalfarm.model.AppUser;
import com.animalfarm.model.RefreshToken;
import com.animalfarm.repository.AppUserRepository;
import com.animalfarm.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionAuthService {
    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public SessionAuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password.");
        }
        return buildLoginResponse(user);
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        Claims claims = parseJwtOrThrow(refreshToken);
        String type = claims.get("typ", String.class);
        if (!"refresh".equals(type)) {
            throw new UnauthorizedException("Invalid refresh token.");
        }
        String hashed = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hashed)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found."));
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            throw new UnauthorizedException("Refresh token expired.");
        }

        stored.setRevoked(true);
        AppUser user = stored.getUser();
        return buildLoginResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String hashed = hashToken(refreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hashed).ifPresent(token -> token.setRevoked(true));
    }

    public AuthSession requireSession(String accessToken) {
        Claims claims = parseJwtOrThrow(accessToken);
        String type = claims.get("typ", String.class);
        if (!"access".equals(type)) {
            throw new UnauthorizedException("Invalid access token.");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        Number ownerIdNum = claims.get("ownerId", Number.class);
        Long ownerId = ownerIdNum != null ? ownerIdNum.longValue() : null;
        Boolean mustChangePassword = claims.get("mustChangePassword", Boolean.class);
        return new AuthSession(
                userId,
                username,
                com.animalfarm.model.ActorRole.valueOf(role),
                ownerId,
                mustChangePassword != null && mustChangePassword
        );
    }

    @Transactional
    public LoginResponse changePassword(String accessToken, ChangePasswordRequest request) {
        AuthSession session = requireSession(accessToken);
        AppUser user = appUserRepository.findById(session.userId())
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setForcePasswordReset(false);
        return buildLoginResponse(user);
    }

    private Claims parseJwtOrThrow(String token) {
        try {
            return jwtTokenService.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired token.");
        }
    }

    private LoginResponse buildLoginResponse(AppUser user) {
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getOwner() != null ? user.getOwner().getId() : null,
                user.isForcePasswordReset()
        );
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId());
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hashToken(refreshToken));
        rt.setExpiresAt(jwtTokenService.getRefreshTokenExpiryTime());
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole(),
                user.getOwner() != null ? user.getOwner().getId() : null,
                user.isForcePasswordReset(),
                jwtTokenService.getAccessTokenMinutes() * 60
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
