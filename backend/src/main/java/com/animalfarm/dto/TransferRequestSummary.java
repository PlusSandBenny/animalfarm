package com.animalfarm.dto;

import com.animalfarm.model.TransferRequest;
import com.animalfarm.model.TransferStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransferRequestSummary(
        Long id,
        UUID fromOwnerId,
        UUID toOwnerId,
        List<UUID> animalIds,
        String ownerEmailMessage,
        TransferStatus status,
        LocalDateTime createdAt
) {
    public static TransferRequestSummary from(TransferRequest tr) {
        return new TransferRequestSummary(
                tr.getId(),
                tr.getFromOwner().getOwnerId(),
                tr.getToOwner().getOwnerId(),
                tr.getAnimalIds(),
                tr.getOwnerEmailMessage(),
                tr.getStatus(),
                tr.getCreatedAt()
        );
    }
}
