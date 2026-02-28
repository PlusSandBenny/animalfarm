package com.animalfarm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record TransferAnimalsRequest(
        @NotNull UUID toOwnerId,
        @NotEmpty List<UUID> animalIds
) {
}
