package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;

public record AuthSession(Long userId, String username, ActorRole role, Long ownerId, boolean mustChangePassword) {
}
