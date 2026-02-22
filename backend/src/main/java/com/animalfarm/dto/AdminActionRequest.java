package com.animalfarm.dto;

import jakarta.validation.constraints.NotNull;

public record AdminActionRequest(@NotNull String actorRole) {
}
