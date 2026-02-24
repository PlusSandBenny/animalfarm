package com.animalfarm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "owner_invoices")
public class OwnerInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(nullable = false)
    private Integer periodMonth;

    @Column(nullable = false)
    private long cattleCount;

    @Column(nullable = false)
    private long goatCount;

    @Column(nullable = false)
    private long ramCount;

    @Column(nullable = false)
    private long pigCount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentCharge;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal previousUnpaidBalance;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDue;

    @Column(nullable = false)
    private boolean paid;

    @Column(nullable = false)
    private boolean emailSent;

    @Column(length = 1000)
    private String emailError;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime sentAt;

    public Long getId() {
        return id;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(Integer periodYear) {
        this.periodYear = periodYear;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(Integer periodMonth) {
        this.periodMonth = periodMonth;
    }

    public long getCattleCount() {
        return cattleCount;
    }

    public void setCattleCount(long cattleCount) {
        this.cattleCount = cattleCount;
    }

    public long getGoatCount() {
        return goatCount;
    }

    public void setGoatCount(long goatCount) {
        this.goatCount = goatCount;
    }

    public long getRamCount() {
        return ramCount;
    }

    public void setRamCount(long ramCount) {
        this.ramCount = ramCount;
    }

    public long getPigCount() {
        return pigCount;
    }

    public void setPigCount(long pigCount) {
        this.pigCount = pigCount;
    }

    public BigDecimal getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(BigDecimal currentCharge) {
        this.currentCharge = currentCharge;
    }

    public BigDecimal getPreviousUnpaidBalance() {
        return previousUnpaidBalance;
    }

    public void setPreviousUnpaidBalance(BigDecimal previousUnpaidBalance) {
        this.previousUnpaidBalance = previousUnpaidBalance;
    }

    public BigDecimal getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(BigDecimal totalDue) {
        this.totalDue = totalDue;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
