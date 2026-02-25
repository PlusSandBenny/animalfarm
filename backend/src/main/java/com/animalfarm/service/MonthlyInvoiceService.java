package com.animalfarm.service;

import com.animalfarm.dto.MonthlyInvoiceResponse;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.AnimalType;
import com.animalfarm.model.InvoiceParameter;
import com.animalfarm.repository.AnimalRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MonthlyInvoiceService {
    private final AnimalRepository animalRepository;
    private final OwnerService ownerService;
    private final InvoiceParameterService invoiceParameterService;

    public MonthlyInvoiceService(
            AnimalRepository animalRepository,
            OwnerService ownerService,
            InvoiceParameterService invoiceParameterService
    ) {
        this.animalRepository = animalRepository;
        this.ownerService = ownerService;
        this.invoiceParameterService = invoiceParameterService;
    }

    public MonthlyInvoiceResponse generateForOwner(Long ownerId, ActorRole role, Long requesterOwnerId) {
        if (role == ActorRole.OWNER && (requesterOwnerId == null || !requesterOwnerId.equals(ownerId))) {
            throw new ApiException("Owner can only generate own monthly invoice.");
        }
        var owner = ownerService.getOwner(ownerId);
        InvoiceParameter p = invoiceParameterService.getInternal();

        long cattle = animalRepository.countByOwnerIdAndTypeAndSoldFalse(ownerId, AnimalType.CATTLE);
        long goats = animalRepository.countByOwnerIdAndTypeAndSoldFalse(ownerId, AnimalType.GOAT);
        long rams = animalRepository.countByOwnerIdAndTypeAndSoldFalse(ownerId, AnimalType.RAM);
        long pigs = animalRepository.countByOwnerIdAndTypeAndSoldFalse(ownerId, AnimalType.PIG);

        BigDecimal cattleRate = p.getCattleMonthlyFeeds().add(p.getCattleMonthlyMedication());
        BigDecimal goatRate = p.getGoatMonthlyFeeds().add(p.getGoatMonthlyMedication());
        BigDecimal ramRate = p.getRamMonthlyFeeds().add(p.getRamMonthlyMedication());
        BigDecimal pigRate = p.getPigMonthlyFeeds().add(p.getPigMonthlyMedication());

        BigDecimal total = cattleRate.multiply(BigDecimal.valueOf(cattle))
                .add(goatRate.multiply(BigDecimal.valueOf(goats)))
                .add(ramRate.multiply(BigDecimal.valueOf(rams)))
                .add(pigRate.multiply(BigDecimal.valueOf(pigs)));

        return new MonthlyInvoiceResponse(
                owner.getId(),
                owner.getFirstName(),
                cattle,
                goats,
                rams,
                pigs,
                total
        );
    }

    public List<MonthlyInvoiceResponse> generateForAllOwners(ActorRole role) {
        RoleValidator.requireAdmin(role);
        return ownerService.listOwners().stream()
                .map(owner -> generateForOwner(owner.getId(), ActorRole.ADMIN, null))
                .toList();
    }
}
