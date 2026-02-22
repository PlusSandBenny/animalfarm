package com.animalfarm.dto;

import com.animalfarm.model.AnimalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AnimalRequest(
        @NotBlank String animalId,
        @NotBlank String color,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String breed,
        @NotNull AnimalType type,
        String image,
        Long parentId,
        @NotNull Long ownerId,
        @NotNull String actorRole
) {
}
