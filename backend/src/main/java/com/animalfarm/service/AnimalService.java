package com.animalfarm.service;

import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Animal;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AnimalRepository;
import com.animalfarm.repository.OwnerRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final OwnerRepository ownerRepository;

    public AnimalService(AnimalRepository animalRepository, OwnerRepository ownerRepository) {
        this.animalRepository = animalRepository;
        this.ownerRepository = ownerRepository;
    }

    public AnimalSummary registerAnimal(AnimalRequest request, ActorRole role) {
        RoleValidator.requireAdmin(role);

        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new ApiException("Owner not found: " + request.ownerId()));

        Animal animal = new Animal();
        animal.setAnimalId(request.animalId());
        animal.setColor(request.color());
        animal.setDateOfBirth(request.dateOfBirth());
        animal.setBreed(request.breed());
        animal.setType(request.type());
        animal.setImage(request.image());
        animal.setParentId(request.parentId());
        animal.setOwner(owner);
        animal.setSold(false);

        return AnimalSummary.from(animalRepository.save(animal));
    }

    public List<AnimalSummary> listAnimals() {
        return animalRepository.findAll().stream().map(AnimalSummary::from).toList();
    }

    public List<AnimalSummary> getByOwner(Long ownerId) {
        return animalRepository.findByOwnerId(ownerId).stream().map(AnimalSummary::from).toList();
    }

    public List<AnimalSummary> getByParent(Long parentId) {
        return animalRepository.findByParentId(parentId).stream().map(AnimalSummary::from).toList();
    }

    @Transactional
    public List<AnimalSummary> transferAnimals(TransferAnimalsRequest request, ActorRole role, Long actorOwnerId) {
        Owner toOwner = ownerRepository.findById(request.toOwnerId())
                .orElseThrow(() -> new ApiException("Destination owner not found: " + request.toOwnerId()));

        List<AnimalSummary> transferred = new ArrayList<>();
        for (Long animalDbId : request.animalIds()) {
            Animal animal = animalRepository.findById(animalDbId)
                    .orElseThrow(() -> new ApiException("Animal not found: " + animalDbId));
            if (animal.isSold()) {
                throw new ApiException("Animal already sold to market: " + animal.getAnimalId());
            }
            boolean isOwner = actorOwnerId != null && animal.getOwner().getId().equals(actorOwnerId);
            if (role != ActorRole.ADMIN && !isOwner) {
                throw new ApiException("Transfer denied. You are not owner of animal id " + animal.getAnimalId());
            }

            animal.setOwner(toOwner);
            transferred.add(AnimalSummary.from(animal));
        }
        return transferred;
    }

    @Transactional
    public AnimalSummary sellAnimalToMarket(Long animalId, ActorRole role) {
        RoleValidator.requireAdmin(role);

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new ApiException("Animal not found: " + animalId));
        animal.setSold(true);
        return AnimalSummary.from(animal);
    }
}
