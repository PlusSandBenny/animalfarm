package com.animalfarm.dto;

import com.animalfarm.model.Owner;

public record OwnerSummary(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        String username
) {
    public static OwnerSummary of(Owner owner, String username) {
        return new OwnerSummary(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getAddress(),
                username
        );
    }
}
