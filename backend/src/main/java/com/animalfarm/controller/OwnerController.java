package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.OwnerRequest;
import com.animalfarm.dto.OwnerSummary;
import com.animalfarm.dto.OwnerUpdateRequest;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.Owner;
import com.animalfarm.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    public Owner registerOwner(@Valid @RequestBody OwnerRequest request, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return ownerService.registerOwner(request, session.role());
    }

    @GetMapping("/{ownerId}")
    public Owner getOwner(@PathVariable Long ownerId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() == ActorRole.OWNER && (session.ownerId() == null || !session.ownerId().equals(ownerId))) {
            throw new ApiException("Owner can only access own profile.");
        }
        return ownerService.getOwner(ownerId);
    }

    @GetMapping("/search")
    public List<OwnerSummary> searchOwners(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String firstName,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        return ownerService.searchOwners(ownerId, firstName, session.role());
    }

    @PutMapping("/{ownerId}")
    public OwnerSummary updateOwner(
            @PathVariable Long ownerId,
            @Valid @RequestBody OwnerUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        return ownerService.updateOwner(ownerId, request, session.role());
    }
}
