package com.animalfarm.auth;

import com.animalfarm.model.ActorRole;
import java.util.UUID;

public record AuthSession(Long userId, String username, ActorRole role, UUID ownerId, boolean mustChangePassword) {
}
