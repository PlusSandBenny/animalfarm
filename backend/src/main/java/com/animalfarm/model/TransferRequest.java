package com.animalfarm.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfer_requests")
public class TransferRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_owner_id", nullable = false)
    private Owner fromOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_owner_id", nullable = false)
    private Owner toOwner;

    @ElementCollection
    @CollectionTable(name = "transfer_request_animals", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "animal_id")
    private List<Long> animalIds = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String ownerEmailMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Owner getFromOwner() {
        return fromOwner;
    }

    public void setFromOwner(Owner fromOwner) {
        this.fromOwner = fromOwner;
    }

    public Owner getToOwner() {
        return toOwner;
    }

    public void setToOwner(Owner toOwner) {
        this.toOwner = toOwner;
    }

    public List<Long> getAnimalIds() {
        return animalIds;
    }

    public void setAnimalIds(List<Long> animalIds) {
        this.animalIds = animalIds;
    }

    public String getOwnerEmailMessage() {
        return ownerEmailMessage;
    }

    public void setOwnerEmailMessage(String ownerEmailMessage) {
        this.ownerEmailMessage = ownerEmailMessage;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
