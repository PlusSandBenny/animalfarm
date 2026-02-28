package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.AnimalType;
import com.animalfarm.service.AnimalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnimalSummary register(
            @RequestParam String color,
            @RequestParam LocalDate dateOfBirth,
            @RequestParam String breed,
            @RequestParam AnimalType type,
            @RequestParam(required = false) UUID parentId,
            @RequestParam UUID ownerId,
            @RequestParam(required = false) MultipartFile image,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        AnimalRequest request = new AnimalRequest(color, dateOfBirth, breed, type, parentId, ownerId);
        return animalService.registerAnimal(request, image, session.role());
    }

    @GetMapping
    public List<AnimalSummary> listAll(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() == ActorRole.OWNER && session.ownerId() != null) {
            return animalService.getByOwner(session.ownerId());
        }
        return animalService.listAnimals();
    }

    @PostMapping("/transfer")
    public List<AnimalSummary> transfer(@Valid @RequestBody TransferAnimalsRequest request, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return animalService.transferAnimals(request, session);
    }

    @PostMapping("/{animalId}/sell")
    public AnimalSummary sell(@PathVariable UUID animalId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return animalService.sellAnimalToMarket(animalId, session);
    }
}
