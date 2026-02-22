package com.animalfarm.controller;

import com.animalfarm.dto.AdminActionRequest;
import com.animalfarm.dto.TransferRequestCreate;
import com.animalfarm.dto.TransferRequestSummary;
import com.animalfarm.service.TransferRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfer-requests")
public class TransferRequestController {
    private final TransferRequestService transferRequestService;

    public TransferRequestController(TransferRequestService transferRequestService) {
        this.transferRequestService = transferRequestService;
    }

    @PostMapping
    public TransferRequestSummary create(@Valid @RequestBody TransferRequestCreate request) {
        return transferRequestService.create(request);
    }

    @GetMapping
    public List<TransferRequestSummary> listAll() {
        return transferRequestService.listAll();
    }

    @PostMapping("/{requestId}/approve")
    public void approve(@PathVariable Long requestId, @Valid @RequestBody AdminActionRequest request) {
        transferRequestService.approve(requestId, request);
    }

    @PostMapping("/{requestId}/reject")
    public void reject(@PathVariable Long requestId, @Valid @RequestBody AdminActionRequest request) {
        transferRequestService.reject(requestId, request);
    }
}
