package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.InvoiceParameterDto;
import com.animalfarm.dto.InvoiceParameterUpdateRequest;
import com.animalfarm.service.InvoiceParameterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice-parameters")
public class InvoiceConfigController {
    private final InvoiceParameterService invoiceParameterService;

    public InvoiceConfigController(InvoiceParameterService invoiceParameterService) {
        this.invoiceParameterService = invoiceParameterService;
    }

    @GetMapping
    public InvoiceParameterDto getCurrent(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return invoiceParameterService.getCurrent(session.role());
    }

    @PutMapping
    public InvoiceParameterDto update(
            @Valid @RequestBody InvoiceParameterUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        return invoiceParameterService.update(request, session.role());
    }
}
