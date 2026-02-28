package com.animalfarm.dto;

import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import java.time.LocalDate;
import java.util.UUID;

public record AnimalSummary(
        Long id,
        UUID animalId,
        String color,
        LocalDate dateOfBirth,
        String breed,
        AnimalType type,
        String image,
        UUID parentId,
        UUID ownerId,
        boolean sold
) {
    public static AnimalSummary from(Animal animal) {
        return new AnimalSummary(
                animal.getId(),
                animal.getAnimalId(),
                animal.getColor(),
                animal.getDateOfBirth(),
                animal.getBreed(),
                animal.getType(),
                animal.getImage(),
                animal.getParentId(),
                animal.getOwner().getOwnerId(),
                animal.isSold()
        );
    }
}
