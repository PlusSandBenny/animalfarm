package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;

public record AuthSession(String username, ActorRole role, Long ownerId) {
}
