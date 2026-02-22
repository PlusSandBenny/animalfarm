package com.animalfarm.controller;

import com.animalfarm.dto.OwnerRequest;
import com.animalfarm.model.Owner;
import com.animalfarm.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    public Owner registerOwner(@Valid @RequestBody OwnerRequest request,
                               @RequestHeader("X-Actor-Role") String actorRole) {
        return ownerService.registerOwner(request, actorRole);
    }

    @GetMapping("/{ownerId}")
    public Owner getOwner(@PathVariable Long ownerId) {
        return ownerService.getOwner(ownerId);
    }
}
