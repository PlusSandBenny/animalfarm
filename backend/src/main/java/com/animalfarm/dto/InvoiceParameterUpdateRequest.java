package com.animalfarm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InvoiceParameterUpdateRequest(
        @NotNull @DecimalMin("0.0") BigDecimal cattleMonthlyFeeds,
        @NotNull @DecimalMin("0.0") BigDecimal cattleMonthlyMedication,
        @NotNull @DecimalMin("0.0") BigDecimal goatMonthlyFeeds,
        @NotNull @DecimalMin("0.0") BigDecimal goatMonthlyMedication,
        @NotNull @DecimalMin("0.0") BigDecimal pigMonthlyFeeds,
        @NotNull @DecimalMin("0.0") BigDecimal pigMonthlyMedication,
        @NotNull @DecimalMin("0.0") BigDecimal ramMonthlyFeeds,
        @NotNull @DecimalMin("0.0") BigDecimal ramMonthlyMedication
) {
}
