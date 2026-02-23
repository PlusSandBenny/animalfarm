package com.animalfarm.repository;

import com.animalfarm.model.Owner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    List<Owner> findByFirstNameContainingIgnoreCase(String firstName);
}
