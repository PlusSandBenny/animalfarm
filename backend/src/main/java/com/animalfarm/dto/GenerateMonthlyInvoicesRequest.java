package com.animalfarm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateMonthlyInvoicesRequest(
        Integer year,
        @Min(1) @Max(12) Integer month
) {
}
