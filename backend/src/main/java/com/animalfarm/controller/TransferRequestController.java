package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.TransferRequestCreate;
import com.animalfarm.dto.TransferRequestSummary;
import com.animalfarm.service.TransferRequestService;
import jakarta.servlet.http.HttpServletRequest;
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
    public TransferRequestSummary create(@Valid @RequestBody TransferRequestCreate request, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return transferRequestService.create(request, session.role(), session.ownerId());
    }

    @GetMapping
    public List<TransferRequestSummary> listAll(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return transferRequestService.listAll(session.role());
    }

    @PostMapping("/{requestId}/approve")
    public void approve(@PathVariable Long requestId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        transferRequestService.approve(requestId, session.role());
    }

    @PostMapping("/{requestId}/reject")
    public void reject(@PathVariable Long requestId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        transferRequestService.reject(requestId, session.role());
    }
}
