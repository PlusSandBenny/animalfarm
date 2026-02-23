package com.animalfarm.auth;

import com.animalfarm.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTH_SESSION_ATTR = "authSession";
    private final SessionAuthService sessionAuthService;

    public AuthInterceptor(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing bearer token.");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        AuthSession session = sessionAuthService.requireSession(token);
        if (session.mustChangePassword()) {
            throw new UnauthorizedException("Password reset required. Use /api/auth/change-password.");
        }
        request.setAttribute(AUTH_SESSION_ATTR, session);
        return true;
    }
}
