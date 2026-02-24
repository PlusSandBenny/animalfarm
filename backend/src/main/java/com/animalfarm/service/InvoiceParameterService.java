package com.animalfarm.service;

import com.animalfarm.dto.InvoiceParameterDto;
import com.animalfarm.dto.InvoiceParameterUpdateRequest;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.InvoiceParameter;
import com.animalfarm.repository.InvoiceParameterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceParameterService {
    private final InvoiceParameterRepository invoiceParameterRepository;

    public InvoiceParameterService(InvoiceParameterRepository invoiceParameterRepository) {
        this.invoiceParameterRepository = invoiceParameterRepository;
    }

    public InvoiceParameterDto getCurrent(ActorRole role) {
        RoleValidator.requireAdmin(role);
        return InvoiceParameterDto.from(getOrCreate());
    }

    @Transactional
    public InvoiceParameterDto update(InvoiceParameterUpdateRequest request, ActorRole role) {
        RoleValidator.requireAdmin(role);
        InvoiceParameter p = getOrCreate();
        p.setCattleMonthlyFeeds(request.cattleMonthlyFeeds());
        p.setCattleMonthlyMedication(request.cattleMonthlyMedication());
        p.setGoatMonthlyFeeds(request.goatMonthlyFeeds());
        p.setGoatMonthlyMedication(request.goatMonthlyMedication());
        p.setPigMonthlyFeeds(request.pigMonthlyFeeds());
        p.setPigMonthlyMedication(request.pigMonthlyMedication());
        p.setRamMonthlyFeeds(request.ramMonthlyFeeds());
        p.setRamMonthlyMedication(request.ramMonthlyMedication());
        return InvoiceParameterDto.from(p);
    }

    public InvoiceParameter getInternal() {
        return getOrCreate();
    }

    private InvoiceParameter getOrCreate() {
        return invoiceParameterRepository.findAll().stream().findFirst().orElseGet(() -> invoiceParameterRepository.save(new InvoiceParameter()));
    }
}
