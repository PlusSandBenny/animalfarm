package com.animalfarm.dto;

import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import java.time.LocalDate;

public record AnimalSummary(
        Long id,
        String animalId,
        String color,
        LocalDate dateOfBirth,
        String breed,
        AnimalType type,
        String image,
        Long parentId,
        Long ownerId,
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
                animal.getOwner().getId(),
                animal.isSold()
        );
    }
}
