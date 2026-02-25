package com.animalfarm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceHistoryResponse(
        Long invoiceId,
        Long ownerId,
        String ownerFirstName,
        String ownerEmail,
        Integer periodYear,
        Integer periodMonth,
        BigDecimal currentCharge,
        BigDecimal previousUnpaidBalance,
        BigDecimal totalDue,
        boolean paid,
        boolean emailSent,
        String emailError,
        LocalDateTime createdAt
) {
}
