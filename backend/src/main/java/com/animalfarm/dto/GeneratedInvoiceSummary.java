package com.animalfarm.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GeneratedInvoiceSummary(
        Long invoiceId,
        UUID ownerId,
        String ownerFirstName,
        String ownerEmail,
        Integer periodYear,
        Integer periodMonth,
        BigDecimal currentCharge,
        BigDecimal previousUnpaidBalance,
        BigDecimal totalDue,
        boolean paid,
        boolean emailSent,
        String emailError
) {
}
