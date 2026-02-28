package com.animalfarm.dto;

import com.animalfarm.model.Owner;
import java.util.UUID;

public record OwnerSummary(
        Long id,
        UUID ownerId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        String username,
        boolean credentialsCreated
) {
    public static OwnerSummary of(Owner owner, String username) {
        return new OwnerSummary(
                owner.getId(),
                owner.getOwnerId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getAddress(),
                username,
                username != null && !username.isBlank()
        );
    }
}
