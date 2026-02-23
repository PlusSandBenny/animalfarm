package com.animalfarm.repository;

import com.animalfarm.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByOwnerId(Long ownerId);
    boolean existsByUsername(String username);
}
