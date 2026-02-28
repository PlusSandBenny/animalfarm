package com.animalfarm.repository;

import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByAnimalId(UUID animalId);
    List<Animal> findByOwnerOwnerId(UUID ownerId);
    List<Animal> findByParentId(UUID parentId);
    long countByOwnerIdAndTypeAndSoldFalse(Long ownerId, AnimalType type);
}
