package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String username,
        ActorRole role,
        Long ownerId,
        long accessTokenExpiresInSeconds
) {
}
