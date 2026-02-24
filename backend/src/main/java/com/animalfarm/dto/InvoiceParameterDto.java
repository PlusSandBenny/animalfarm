package com.animalfarm.dto;

import com.animalfarm.model.InvoiceParameter;
import java.math.BigDecimal;

public record InvoiceParameterDto(
        BigDecimal cattleMonthlyFeeds,
        BigDecimal cattleMonthlyMedication,
        BigDecimal goatMonthlyFeeds,
        BigDecimal goatMonthlyMedication,
        BigDecimal pigMonthlyFeeds,
        BigDecimal pigMonthlyMedication,
        BigDecimal ramMonthlyFeeds,
        BigDecimal ramMonthlyMedication
) {
    public static InvoiceParameterDto from(InvoiceParameter p) {
        return new InvoiceParameterDto(
                p.getCattleMonthlyFeeds(),
                p.getCattleMonthlyMedication(),
                p.getGoatMonthlyFeeds(),
                p.getGoatMonthlyMedication(),
                p.getPigMonthlyFeeds(),
                p.getPigMonthlyMedication(),
                p.getRamMonthlyFeeds(),
                p.getRamMonthlyMedication()
        );
    }
}
