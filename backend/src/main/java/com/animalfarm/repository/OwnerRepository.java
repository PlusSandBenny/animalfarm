package com.animalfarm.repository;

import com.animalfarm.model.Owner;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByOwnerId(UUID ownerId);
    List<Owner> findByFirstNameContainingIgnoreCase(String firstName);
}
