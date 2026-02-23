package com.animalfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.animalfarm.dto.TransferRequestCreate;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Owner;
import com.animalfarm.model.TransferRequest;
import com.animalfarm.model.TransferStatus;
import com.animalfarm.repository.TransferRequestRepository;
import java.util.List;
import java.util.Optional;
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
        Owner fromOwner = owner(1L);
        Owner toOwner = owner(2L);
        TransferRequest saved = transferRequest(fromOwner, toOwner, List.of(10L, 11L), TransferStatus.PENDING);
        ReflectionTestUtils.setField(saved, "id", 100L);

        when(ownerService.getOwner(1L)).thenReturn(fromOwner);
        when(ownerService.getOwner(2L)).thenReturn(toOwner);
        when(transferRequestRepository.save(any(TransferRequest.class))).thenReturn(saved);

        var result = transferRequestService.create(new TransferRequestCreate(
                1L, 2L, List.of(10L, 11L), "Please transfer these animals"
        ), ActorRole.OWNER, 1L);

        assertEquals(100L, result.id());
        assertEquals(TransferStatus.PENDING, result.status());
    }

    @Test
    void approve_byAdmin_setsStatusAndInvokesAnimalTransfer() {
        Owner fromOwner = owner(1L);
        Owner toOwner = owner(2L);
        TransferRequest request = transferRequest(fromOwner, toOwner, List.of(50L), TransferStatus.PENDING);
        ReflectionTestUtils.setField(request, "id", 5L);

        when(transferRequestRepository.findById(5L)).thenReturn(Optional.of(request));

        transferRequestService.approve(5L, ActorRole.ADMIN);

        verify(animalService).transferAnimals(any(), any(), any());
        assertEquals(TransferStatus.APPROVED, request.getStatus());
    }

    @Test
    void reject_requiresAdminRole() {
        ApiException ex = assertThrows(ApiException.class, () ->
                transferRequestService.reject(3L, ActorRole.OWNER));

        assertEquals("This action requires ADMIN role.", ex.getMessage());
    }

    private static Owner owner(Long id) {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", id);
        owner.setFirstName("Owner");
        owner.setLastName("User");
        owner.setEmail("owner" + id + "@mail.com");
        owner.setPhoneNumber("0700000");
        owner.setAddress("Addr");
        return owner;
    }

    private static TransferRequest transferRequest(Owner from, Owner to, List<Long> animalIds, TransferStatus status) {
        TransferRequest request = new TransferRequest();
        request.setFromOwner(from);
        request.setToOwner(to);
        request.setAnimalIds(animalIds);
        request.setOwnerEmailMessage("mail body");
        request.setStatus(status);
        return request;
    }
}
