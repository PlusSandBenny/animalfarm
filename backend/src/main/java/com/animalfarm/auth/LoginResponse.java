package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;

public record LoginResponse(String token, String username, ActorRole role, Long ownerId) {
}
