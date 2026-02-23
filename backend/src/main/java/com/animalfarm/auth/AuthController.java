package com.animalfarm.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SessionAuthService sessionAuthService;

    public AuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return sessionAuthService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return sessionAuthService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) LogoutRequest request) {
        sessionAuthService.logout(request != null ? request.refreshToken() : null);
    }

    @PostMapping("/change-password")
    public LoginResponse changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return sessionAuthService.changePassword(extractBearerToken(authorization), request);
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new com.animalfarm.exception.UnauthorizedException("Missing bearer token.");
        }
        return authorization.substring("Bearer ".length()).trim();
    }
}
