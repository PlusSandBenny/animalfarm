package com.animalfarm.controller;

import com.animalfarm.dto.AdminActionRequest;
import com.animalfarm.dto.AnimalRequest;
import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.dto.TransferAnimalsRequest;
import com.animalfarm.service.AnimalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    public AnimalSummary register(@Valid @RequestBody AnimalRequest request) {
        return animalService.registerAnimal(request);
    }

    @GetMapping
    public List<AnimalSummary> listAll() {
        return animalService.listAnimals();
    }

    @PostMapping("/transfer")
    public List<AnimalSummary> transfer(@Valid @RequestBody TransferAnimalsRequest request) {
        return animalService.transferAnimals(request);
    }

    @PostMapping("/{animalId}/sell")
    public AnimalSummary sell(@PathVariable Long animalId, @Valid @RequestBody AdminActionRequest request) {
        return animalService.sellAnimalToMarket(animalId, request.actorRole());
    }
}
