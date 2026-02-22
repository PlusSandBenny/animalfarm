package com.animalfarm.service;

import com.animalfarm.dto.OwnerRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.OwnerRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public Owner registerOwner(OwnerRequest request, String actorRole) {
        ActorRole role = RoleValidator.parseRole(actorRole);
        RoleValidator.requireAdmin(role);

        Owner owner = new Owner();
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setEmail(request.email());
        owner.setPhoneNumber(request.phoneNumber());
        owner.setAddress(request.address());
        return ownerRepository.save(owner);
    }

    public Owner getOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException("Owner not found: " + ownerId));
    }
}
