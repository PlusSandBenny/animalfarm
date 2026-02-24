package com.animalfarm.repository;

import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByOwnerId(Long ownerId);
    List<Animal> findByParentId(Long parentId);
    long countByOwnerIdAndTypeAndSoldFalse(Long ownerId, AnimalType type);
}
