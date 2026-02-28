package com.animalfarm.service;

import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Animal;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AnimalRepository;
import com.animalfarm.repository.OwnerRepository;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final OwnerRepository ownerRepository;
    private final AuditLogService auditLogService;

    public AnimalService(
            AnimalRepository animalRepository,
            OwnerRepository ownerRepository,
            AuditLogService auditLogService
    ) {
        this.animalRepository = animalRepository;
        this.ownerRepository = ownerRepository;
        this.auditLogService = auditLogService;
    }

    public AnimalSummary registerAnimal(AnimalRequest request, MultipartFile imageFile, ActorRole role) {
        RoleValidator.requireAdmin(role);

        Owner owner = ownerRepository.findByOwnerId(request.ownerId())
                .orElseThrow(() -> new ApiException("Owner not found: " + request.ownerId()));

        Animal animal = new Animal();
        animal.setColor(request.color());
        animal.setDateOfBirth(request.dateOfBirth());
        animal.setBreed(request.breed());
        animal.setType(request.type());
        animal.setImage(toImageDataUrl(imageFile));
        animal.setParentId(request.parentId());
        animal.setOwner(owner);
        animal.setSold(false);

        return AnimalSummary.from(animalRepository.save(animal));
    }

    public List<AnimalSummary> listAnimals() {
        return animalRepository.findAll().stream().map(AnimalSummary::from).toList();
    }

    public List<AnimalSummary> getByOwner(UUID ownerId) {
        return animalRepository.findByOwnerOwnerId(ownerId).stream().map(AnimalSummary::from).toList();
    }

    public List<AnimalSummary> getByParent(UUID parentId) {
        return animalRepository.findByParentId(parentId).stream().map(AnimalSummary::from).toList();
    }

    @Transactional
    public List<AnimalSummary> transferAnimals(TransferAnimalsRequest request, AuthSession actor) {
        ActorRole role = actor.role();
        UUID actorOwnerId = actor.ownerId();
        Owner toOwner = ownerRepository.findByOwnerId(request.toOwnerId())
                .orElseThrow(() -> new ApiException("Destination owner not found: " + request.toOwnerId()));

        List<AnimalSummary> transferred = new ArrayList<>();
        for (UUID animalUuid : request.animalIds()) {
            Animal animal = animalRepository.findByAnimalId(animalUuid)
                    .orElseThrow(() -> new ApiException("Animal not found: " + animalUuid));
            if (animal.isSold()) {
                throw new ApiException("Animal already sold to market: " + animal.getAnimalId());
            }
            boolean isOwner = actorOwnerId != null && animal.getOwner().getOwnerId().equals(actorOwnerId);
            if (role != ActorRole.ADMIN && !isOwner) {
                throw new ApiException("Transfer denied. You are not owner of animal id " + animal.getAnimalId());
            }

            animal.setOwner(toOwner);
            transferred.add(AnimalSummary.from(animal));
        }
        auditLogService.log(actor, "TRANSFER_ANIMALS",
                "Transferred animals " + request.animalIds() + " to owner " + request.toOwnerId());
        return transferred;
    }

    @Transactional
    public AnimalSummary sellAnimalToMarket(UUID animalId, AuthSession actor) {
        ActorRole role = actor.role();
        RoleValidator.requireAdmin(role);

        Animal animal = animalRepository.findByAnimalId(animalId)
                .orElseThrow(() -> new ApiException("Animal not found: " + animalId));
        animal.setSold(true);
        auditLogService.log(actor, "SELL_ANIMAL",
                "Sold animal " + animal.getAnimalId() + " (dbId " + animal.getId() + ") to market");
        return AnimalSummary.from(animal);
    }

    private String toImageDataUrl(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String contentType = imageFile.getContentType() != null ? imageFile.getContentType() : "application/octet-stream";
        try {
            String base64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
            return "data:" + contentType + ";base64," + base64;
        } catch (Exception e) {
            throw new ApiException("Failed to read uploaded image.");
        }
    }
}
