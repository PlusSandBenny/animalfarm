package com.animalfarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TransferRequestCreate(
        @NotNull UUID fromOwnerId,
        @NotNull UUID toOwnerId,
        @NotEmpty List<UUID> animalIds,
        @NotBlank String ownerEmailMessage
) {
}
