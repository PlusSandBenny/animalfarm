package com.animalfarm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TransferAnimalsRequest(
        @NotNull Long toOwnerId,
        @NotEmpty List<Long> animalIds
) {
}
