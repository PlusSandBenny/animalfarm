package com.animalfarm.auth;

import com.animalfarm.exception.UnauthorizedException;
import com.animalfarm.model.ActorRole;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SessionAuthService {
    private final Map<String, UserAccount> users = Map.of(
            "admin", new UserAccount("admin123", ActorRole.ADMIN, null),
            "owner1", new UserAccount("owner123", ActorRole.OWNER, 1L),
            "owner2", new UserAccount("owner123", ActorRole.OWNER, 2L)
    );

    private final ConcurrentHashMap<String, AuthSession> sessions = new ConcurrentHashMap<>();

    public LoginResponse login(LoginRequest request) {
        UserAccount account = users.get(request.username());
        if (account == null || !account.password().equals(request.password())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        String token = UUID.randomUUID().toString();
        AuthSession session = new AuthSession(request.username(), account.role(), account.ownerId());
        sessions.put(token, session);
        return new LoginResponse(token, request.username(), account.role(), account.ownerId());
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public AuthSession requireSession(String token) {
        AuthSession session = sessions.get(token);
        if (session == null) {
            throw new UnauthorizedException("Invalid or expired token.");
        }
        return session;
    }

    private record UserAccount(String password, ActorRole role, Long ownerId) {
    }
}
