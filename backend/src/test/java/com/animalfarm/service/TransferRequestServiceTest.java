package com.animalfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.animalfarm.dto.TransferRequestCreate;
import com.animalfarm.exception.ApiException;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Owner;
import com.animalfarm.model.TransferRequest;
import com.animalfarm.model.TransferStatus;
import com.animalfarm.repository.TransferRequestRepository;
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
class TransferRequestServiceTest {

    @Mock
    private TransferRequestRepository transferRequestRepository;

    @Mock
    private OwnerService ownerService;

    @Mock
    private AnimalService animalService;

    private TransferRequestService transferRequestService;

    @BeforeEach
    void setUp() {
        transferRequestService = new TransferRequestService(transferRequestRepository, ownerService, animalService);
    }

    @Test
    void create_acceptsOwnerOrAdminRole() {
        Owner fromOwner = owner(1L, UUID.randomUUID());
        Owner toOwner = owner(2L, UUID.randomUUID());
        TransferRequest saved = transferRequest(fromOwner, toOwner, List.of(UUID.randomUUID(), UUID.randomUUID()), TransferStatus.PENDING);
        ReflectionTestUtils.setField(saved, "id", 100L);

        when(ownerService.getOwner(fromOwner.getOwnerId())).thenReturn(fromOwner);
        when(ownerService.getOwner(toOwner.getOwnerId())).thenReturn(toOwner);
        when(transferRequestRepository.save(any(TransferRequest.class))).thenReturn(saved);

        var result = transferRequestService.create(new TransferRequestCreate(
                fromOwner.getOwnerId(), toOwner.getOwnerId(), List.of(UUID.randomUUID(), UUID.randomUUID()), "Please transfer these animals"
        ), ActorRole.OWNER, fromOwner.getOwnerId());

        assertEquals(100L, result.id());
        assertEquals(TransferStatus.PENDING, result.status());
    }

    @Test
    void approve_byAdmin_setsStatusAndInvokesAnimalTransfer() {
        Owner fromOwner = owner(1L, UUID.randomUUID());
        Owner toOwner = owner(2L, UUID.randomUUID());
        TransferRequest request = transferRequest(fromOwner, toOwner, List.of(UUID.randomUUID()), TransferStatus.PENDING);
        ReflectionTestUtils.setField(request, "id", 5L);

        when(transferRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        transferRequestService.approve(5L, new AuthSession(1L, "admin", ActorRole.ADMIN, null, false));

        verify(animalService).transferAnimals(any(), any());
        assertEquals(TransferStatus.APPROVED, request.getStatus());
    }

    @Test
    void reject_requiresAdminRole() {
        ApiException ex = assertThrows(ApiException.class, () ->
                transferRequestService.reject(3L, new AuthSession(2L, "owner", ActorRole.OWNER, UUID.randomUUID(), false)));

        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    private static Owner owner(Long id, UUID ownerId) {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", id);
        ReflectionTestUtils.setField(owner, "ownerId", ownerId);
        owner.setFirstName("Owner");
        owner.setLastName("User");
        owner.setEmail("owner" + id + "@mail.com");
        owner.setPhoneNumber("0700000");
        owner.setAddress("Addr");
        return owner;
    }

    private static TransferRequest transferRequest(Owner from, Owner to, List<UUID> animalIds, TransferStatus status) {
        TransferRequest request = new TransferRequest();
        request.setFromOwner(from);
        request.setToOwner(to);
        request.setAnimalIds(animalIds);
        request.setOwnerEmailMessage("mail body");
        request.setStatus(status);
        return request;
    }
}
