package com.animalfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Animal;
import com.animalfarm.model.AnimalType;
import com.animalfarm.model.Owner;
import com.animalfarm.repository.AnimalRepository;
import com.animalfarm.repository.OwnerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    private AnimalService animalService;

    @BeforeEach
    void setUp() {
        animalService = new AnimalService(animalRepository, ownerRepository);
    }

    @Test
    void registerAnimal_requiresAdminRole() {
        AnimalRequest request = new AnimalRequest(
                "A-001",
                "Brown",
                LocalDate.of(2022, 1, 1),
                "Boran",
                AnimalType.CATTLE,
                null,
                null,
                1L
        );

        ApiException ex = assertThrows(ApiException.class, () -> animalService.registerAnimal(request, ActorRole.OWNER));
        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    @Test
    void transferAnimals_ownerCanTransferOwnAnimal() {
        Owner fromOwner = owner(1L);
        Owner toOwner = owner(2L);
        Animal animal = animal(10L, "A-010", fromOwner, false);

        when(ownerRepository.findById(2L)).thenReturn(Optional.of(toOwner));
        when(animalRepository.findById(10L)).thenReturn(Optional.of(animal));

        var result = animalService.transferAnimals(new TransferAnimalsRequest(
                2L,
                List.of(10L)
        ), ActorRole.OWNER, 1L);

        assertEquals(1, result.size());
        assertEquals(2L, animal.getOwner().getId());
        assertEquals(2L, result.get(0).ownerId());
    }

    @Test
    void transferAnimals_nonOwnerDeniedWhenNotAdmin() {
        Owner realOwner = owner(1L);
        Owner toOwner = owner(2L);
        Animal animal = animal(11L, "A-011", realOwner, false);

        when(ownerRepository.findById(2L)).thenReturn(Optional.of(toOwner));
        when(animalRepository.findById(11L)).thenReturn(Optional.of(animal));

        ApiException ex = assertThrows(ApiException.class, () -> animalService.transferAnimals(new TransferAnimalsRequest(
                2L,
                List.of(11L)
        ), ActorRole.OWNER, 99L));

        assertEquals("Transfer denied. You are not owner of animal id A-011", ex.getMessage());
    }

    @Test
    void sellAnimal_requiresAdminRole() {
        ApiException ex = assertThrows(ApiException.class, () -> animalService.sellAnimalToMarket(12L, ActorRole.OWNER));
        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    private static Owner owner(Long id) {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", id);
        owner.setFirstName("First");
        owner.setLastName("Last");
        owner.setEmail("owner" + id + "@example.com");
        owner.setPhoneNumber("123");
        owner.setAddress("addr");
        return owner;
    }

    private static Animal animal(Long id, String animalId, Owner owner, boolean sold) {
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
