package com.animalfarm.dto;

import java.math.BigDecimal;

public record MonthlyInvoiceResponse(
        Long ownerId,
        String firstName,
        long cattleCount,
        long goatCount,
        long ramCount,
        long pigCount,
        BigDecimal totalAmount
) {
}
