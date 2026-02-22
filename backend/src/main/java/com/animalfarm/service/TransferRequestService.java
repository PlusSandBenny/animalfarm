package com.animalfarm.service;

import com.animalfarm.dto.AdminActionRequest;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.dto.TransferRequestCreate;
import com.animalfarm.dto.TransferRequestSummary;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.TransferRequest;
import com.animalfarm.model.TransferStatus;
import com.animalfarm.repository.TransferRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferRequestService {
    private final TransferRequestRepository transferRequestRepository;
    private final OwnerService ownerService;
    private final AnimalService animalService;

    public TransferRequestService(
            TransferRequestRepository transferRequestRepository,
            OwnerService ownerService,
            AnimalService animalService
    ) {
        this.transferRequestRepository = transferRequestRepository;
        this.ownerService = ownerService;
        this.animalService = animalService;
    }

    public TransferRequestSummary create(TransferRequestCreate request) {
        ActorRole role = RoleValidator.parseRole(request.actorRole());
        if (role != ActorRole.OWNER && role != ActorRole.ADMIN) {
            throw new ApiException("Only OWNER or ADMIN can create transfer requests.");
        }

        TransferRequest tr = new TransferRequest();
        tr.setFromOwner(ownerService.getOwner(request.fromOwnerId()));
        tr.setToOwner(ownerService.getOwner(request.toOwnerId()));
        tr.setAnimalIds(request.animalIds());
        tr.setOwnerEmailMessage(request.ownerEmailMessage());
        tr.setStatus(TransferStatus.PENDING);
        return TransferRequestSummary.from(transferRequestRepository.save(tr));
    }

    public List<TransferRequestSummary> listAll() {
        return transferRequestRepository.findAll().stream().map(TransferRequestSummary::from).toList();
    }

    @Transactional
    public void approve(Long requestId, AdminActionRequest request) {
        ActorRole role = RoleValidator.parseRole(request.actorRole());
        RoleValidator.requireAdmin(role);

        TransferRequest tr = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException("Transfer request not found: " + requestId));
        if (tr.getStatus() != TransferStatus.PENDING) {
            throw new ApiException("Transfer request is already processed.");
        }

        animalService.transferAnimals(new TransferAnimalsRequest(
                tr.getToOwner().getId(),
                tr.getAnimalIds(),
                tr.getFromOwner().getId(),
                ActorRole.ADMIN.name()
        ));
        tr.setStatus(TransferStatus.APPROVED);
    }

    @Transactional
    public void reject(Long requestId, AdminActionRequest request) {
        ActorRole role = RoleValidator.parseRole(request.actorRole());
        RoleValidator.requireAdmin(role);

        TransferRequest tr = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException("Transfer request not found: " + requestId));
        if (tr.getStatus() != TransferStatus.PENDING) {
            throw new ApiException("Transfer request is already processed.");
        }
        tr.setStatus(TransferStatus.REJECTED);
    }
}
