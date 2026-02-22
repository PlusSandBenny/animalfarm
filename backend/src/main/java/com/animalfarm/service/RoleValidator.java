package com.animalfarm.service;

import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;

public final class RoleValidator {
    private RoleValidator() {
    }

    public static ActorRole parseRole(String role) {
        try {
            return ActorRole.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            throw new ApiException("Invalid actor role. Use OWNER or ADMIN.");
        }
    }

    public static void requireAdmin(ActorRole role) {
        if (role != ActorRole.ADMIN) {
            throw new ApiException("This action requires ADMIN role.");
        }
    }
}
