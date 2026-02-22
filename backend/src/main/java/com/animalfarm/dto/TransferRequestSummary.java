package com.animalfarm.dto;

import com.animalfarm.model.TransferRequest;
import com.animalfarm.model.TransferStatus;
import java.time.LocalDateTime;
import java.util.List;

public record TransferRequestSummary(
        Long id,
        Long fromOwnerId,
        Long toOwnerId,
        List<Long> animalIds,
        String ownerEmailMessage,
        TransferStatus status,
        LocalDateTime createdAt
) {
    public static TransferRequestSummary from(TransferRequest tr) {
        return new TransferRequestSummary(
                tr.getId(),
                tr.getFromOwner().getId(),
                tr.getToOwner().getId(),
                tr.getAnimalIds(),
                tr.getOwnerEmailMessage(),
                tr.getStatus(),
                tr.getCreatedAt()
        );
    }
}
