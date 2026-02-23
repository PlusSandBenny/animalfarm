package com.animalfarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TransferRequestCreate(
        @NotNull Long fromOwnerId,
        @NotNull Long toOwnerId,
        @NotEmpty List<Long> animalIds,
        @NotBlank String ownerEmailMessage
) {
}
