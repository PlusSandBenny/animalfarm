package com.animalfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AnimalRepository;
import com.animalfarm.repository.OwnerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private AuditLogService auditLogService;

    private AnimalService animalService;

    @BeforeEach
    void setUp() {
        animalService = new AnimalService(animalRepository, ownerRepository, auditLogService);
    }

    @Test
    void registerAnimal_requiresAdminRole() {
        UUID ownerUuid = UUID.randomUUID();
        AnimalRequest request = new AnimalRequest(
                "Brown",
                LocalDate.of(2022, 1, 1),
                "Boran",
                AnimalType.CATTLE,
                null,
                ownerUuid
        );

        ApiException ex = assertThrows(ApiException.class, () -> animalService.registerAnimal(request, null, ActorRole.OWNER));
        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    @Test
    void transferAnimals_ownerCanTransferOwnAnimal() {
        Owner fromOwner = owner(1L, UUID.randomUUID());
        Owner toOwner = owner(2L, UUID.randomUUID());
        UUID animalUuid = UUID.randomUUID();
        Animal animal = animal(10L, animalUuid, fromOwner, false);

        when(ownerRepository.findByOwnerId(toOwner.getOwnerId())).thenReturn(Optional.of(toOwner));
        when(animalRepository.findByAnimalId(animalUuid)).thenReturn(Optional.of(animal));

        var result = animalService.transferAnimals(new TransferAnimalsRequest(
                toOwner.getOwnerId(),
                List.of(animalUuid)
        ), new AuthSession(1L, "owner1", ActorRole.OWNER, fromOwner.getOwnerId(), false));

        assertEquals(1, result.size());
        assertEquals(2L, animal.getOwner().getId());
        assertEquals(toOwner.getOwnerId(), result.get(0).ownerId());
    }

    @Test
    void transferAnimals_nonOwnerDeniedWhenNotAdmin() {
        Owner realOwner = owner(1L, UUID.randomUUID());
        Owner toOwner = owner(2L, UUID.randomUUID());
        UUID animalUuid = UUID.randomUUID();
        Animal animal = animal(11L, animalUuid, realOwner, false);

        when(ownerRepository.findByOwnerId(toOwner.getOwnerId())).thenReturn(Optional.of(toOwner));
        when(animalRepository.findByAnimalId(animalUuid)).thenReturn(Optional.of(animal));

        ApiException ex = assertThrows(ApiException.class, () -> animalService.transferAnimals(new TransferAnimalsRequest(
                toOwner.getOwnerId(),
                List.of(animalUuid)
        ), new AuthSession(2L, "owner2", ActorRole.OWNER, UUID.randomUUID(), false)));

        assertEquals("Transfer denied. You are not owner of animal id " + animalUuid, ex.getMessage());
    }

    @Test
    void sellAnimal_requiresAdminRole() {
        ApiException ex = assertThrows(ApiException.class, () ->
                animalService.sellAnimalToMarket(UUID.randomUUID(), new AuthSession(1L, "owner1", ActorRole.OWNER, UUID.randomUUID(), false)));
        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    private static Owner owner(Long id, UUID ownerId) {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", id);
        ReflectionTestUtils.setField(owner, "ownerId", ownerId);
        owner.setFirstName("First");
        owner.setLastName("Last");
        owner.setEmail("owner" + id + "@example.com");
        owner.setPhoneNumber("123");
        owner.setAddress("addr");
        return owner;
    }

    private static Animal animal(Long id, UUID animalId, Owner owner, boolean sold) {
        Animal animal = new Animal();
        ReflectionTestUtils.setField(animal, "id", id);
        animal.setAnimalId(animalId);
        animal.setColor("Black");
        animal.setDateOfBirth(LocalDate.of(2020, 5, 10));
        animal.setBreed("Breed");
        animal.setType(AnimalType.GOAT);
        animal.setOwner(owner);
        animal.setSold(sold);
        return animal;
    }
}
