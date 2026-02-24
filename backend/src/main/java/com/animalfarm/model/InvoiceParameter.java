package com.animalfarm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_parameters")
public class InvoiceParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cattleMonthlyFeeds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cattleMonthlyMedication = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal goatMonthlyFeeds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal goatMonthlyMedication = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pigMonthlyFeeds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pigMonthlyMedication = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal ramMonthlyFeeds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal ramMonthlyMedication = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public BigDecimal getCattleMonthlyFeeds() {
        return cattleMonthlyFeeds;
    }

    public void setCattleMonthlyFeeds(BigDecimal cattleMonthlyFeeds) {
        this.cattleMonthlyFeeds = cattleMonthlyFeeds;
    }

    public BigDecimal getCattleMonthlyMedication() {
        return cattleMonthlyMedication;
    }

    public void setCattleMonthlyMedication(BigDecimal cattleMonthlyMedication) {
        this.cattleMonthlyMedication = cattleMonthlyMedication;
    }

    public BigDecimal getGoatMonthlyFeeds() {
        return goatMonthlyFeeds;
    }

    public void setGoatMonthlyFeeds(BigDecimal goatMonthlyFeeds) {
        this.goatMonthlyFeeds = goatMonthlyFeeds;
    }

    public BigDecimal getGoatMonthlyMedication() {
        return goatMonthlyMedication;
    }

    public void setGoatMonthlyMedication(BigDecimal goatMonthlyMedication) {
        this.goatMonthlyMedication = goatMonthlyMedication;
    }

    public BigDecimal getPigMonthlyFeeds() {
        return pigMonthlyFeeds;
    }

    public void setPigMonthlyFeeds(BigDecimal pigMonthlyFeeds) {
        this.pigMonthlyFeeds = pigMonthlyFeeds;
    }

    public BigDecimal getPigMonthlyMedication() {
        return pigMonthlyMedication;
    }

    public void setPigMonthlyMedication(BigDecimal pigMonthlyMedication) {
        this.pigMonthlyMedication = pigMonthlyMedication;
    }

    public BigDecimal getRamMonthlyFeeds() {
        return ramMonthlyFeeds;
    }

    public void setRamMonthlyFeeds(BigDecimal ramMonthlyFeeds) {
        this.ramMonthlyFeeds = ramMonthlyFeeds;
    }

    public BigDecimal getRamMonthlyMedication() {
        return ramMonthlyMedication;
    }

    public void setRamMonthlyMedication(BigDecimal ramMonthlyMedication) {
        this.ramMonthlyMedication = ramMonthlyMedication;
    }
}
