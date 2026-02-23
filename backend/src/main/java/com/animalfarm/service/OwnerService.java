package com.animalfarm.service;

import com.animalfarm.dto.OwnerRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.AppUser;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AppUserRepository;
import com.animalfarm.repository.OwnerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerService(
            OwnerRepository ownerRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.ownerRepository = ownerRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Owner registerOwner(OwnerRequest request, ActorRole role) {
        RoleValidator.requireAdmin(role);
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ApiException("Username already exists: " + request.username());
        }

        Owner owner = new Owner();
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setEmail(request.email());
        owner.setPhoneNumber(request.phoneNumber());
        owner.setAddress(request.address());
        Owner savedOwner = ownerRepository.save(owner);

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(ActorRole.OWNER);
        user.setOwner(savedOwner);
        appUserRepository.save(user);
        return savedOwner;
    }

    public Owner getOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException("Owner not found: " + ownerId));
    }
}
