package com.animalfarm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @NotBlank String phoneNumber,
        @NotBlank String address,
        @NotBlank String username,
        @NotBlank String password
) {
}
