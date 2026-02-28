package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String username,
        ActorRole role,
        UUID ownerId,
        boolean mustChangePassword,
        long accessTokenExpiresInSeconds
) {
}
