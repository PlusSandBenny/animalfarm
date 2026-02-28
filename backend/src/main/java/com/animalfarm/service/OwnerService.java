package com.animalfarm.service;

import com.animalfarm.dto.OwnerRequest;
import com.animalfarm.dto.OwnerSummary;
import com.animalfarm.dto.OwnerUpdateRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.AppUser;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AppUserRepository;
import com.animalfarm.repository.OwnerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

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
        user.setForcePasswordReset(true);
        appUserRepository.save(user);
        return savedOwner;
    }

    public Owner getOwner(UUID ownerId) {
        return ownerRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ApiException("Owner not found: " + ownerId));
    }

    public List<Owner> listOwners() {
        return ownerRepository.findAll();
    }

    public List<OwnerSummary> searchOwners(UUID ownerId, String firstName, ActorRole role) {
        RoleValidator.requireAdmin(role);
        if (ownerId != null) {
            return List.of(toSummary(getOwner(ownerId)));
        }
        if (firstName != null && !firstName.isBlank()) {
            return ownerRepository.findByFirstNameContainingIgnoreCase(firstName.trim())
                    .stream()
                    .map(this::toSummary)
                    .toList();
        }
        return ownerRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional
    public OwnerSummary updateOwner(UUID ownerId, OwnerUpdateRequest request, ActorRole role) {
        RoleValidator.requireAdmin(role);
        Owner owner = getOwner(ownerId);
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setEmail(request.email());
        owner.setPhoneNumber(request.phoneNumber());
        owner.setAddress(request.address());
        AppUser user = appUserRepository.findByOwnerOwnerId(ownerId).orElse(null);
        String requestedUsername = request.username() != null ? request.username().trim() : "";
        String requestedTempPassword = request.temporaryPassword() != null ? request.temporaryPassword().trim() : "";

        if (user == null) {
            if (requestedUsername.isEmpty() || requestedTempPassword.isEmpty()) {
                throw new ApiException("Owner has no username. Provide username and temporaryPassword.");
            }
            if (appUserRepository.existsByUsername(requestedUsername)) {
                throw new ApiException("Username already exists: " + requestedUsername);
            }
            AppUser newUser = new AppUser();
            newUser.setUsername(requestedUsername);
            newUser.setPasswordHash(passwordEncoder.encode(requestedTempPassword));
            newUser.setRole(ActorRole.OWNER);
            newUser.setOwner(owner);
            newUser.setForcePasswordReset(true);
            appUserRepository.save(newUser);
        } else {
            if (!requestedUsername.isEmpty() && !requestedUsername.equals(user.getUsername())) {
                throw new ApiException("Username cannot be changed for existing owner account.");
            }
            if (!requestedTempPassword.isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(requestedTempPassword));
                user.setForcePasswordReset(true);
            }
        }
        return toSummary(owner);
    }

    private OwnerSummary toSummary(Owner owner) {
        String username = appUserRepository.findByOwnerOwnerId(owner.getOwnerId())
                .map(AppUser::getUsername)
                .orElse(null);
        return OwnerSummary.of(owner, username);
    }
}
