package com.animalfarm.auth;

import com.animalfarm.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthContext {
    private AuthContext() {
    }

    public static AuthSession require(HttpServletRequest request) {
        Object session = request.getAttribute(AuthInterceptor.AUTH_SESSION_ATTR);
        if (session instanceof AuthSession authSession) {
            return authSession;
        }
        throw new UnauthorizedException("Authentication context not found.");
    }
}
