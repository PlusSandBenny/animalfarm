package com.animalfarm.dto;

import com.animalfarm.model.AnimalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AnimalRequest(
        @NotBlank String color,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String breed,
        @NotNull AnimalType type,
        UUID parentId,
        @NotNull UUID ownerId
) {
}
