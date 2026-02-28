package com.animalfarm.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyInvoiceResponse(
        UUID ownerId,
        String firstName,
        long cattleCount,
        long goatCount,
        long ramCount,
        long pigCount,
        BigDecimal totalAmount
) {
}
