package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.GenerateMonthlyInvoicesRequest;
import com.animalfarm.dto.GeneratedInvoiceSummary;
import com.animalfarm.dto.MonthlyInvoiceResponse;
import com.animalfarm.service.MonthlyInvoiceService;
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
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final MonthlyInvoiceService monthlyInvoiceService;

    public InvoiceController(MonthlyInvoiceService monthlyInvoiceService) {
        this.monthlyInvoiceService = monthlyInvoiceService;
    }

    @GetMapping("/monthly/owner/{ownerId}")
    public MonthlyInvoiceResponse monthlyOwnerInvoice(@PathVariable Long ownerId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return monthlyInvoiceService.generateForOwner(ownerId, session.role(), session.ownerId());
    }

    @GetMapping("/monthly/owners")
    public List<MonthlyInvoiceResponse> monthlyAllOwnersInvoice(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        return monthlyInvoiceService.generateForAllOwners(session.role());
    }

    @PostMapping("/monthly/generate-and-email")
    public List<GeneratedInvoiceSummary> generateAndEmail(
            @Valid @RequestBody(required = false) GenerateMonthlyInvoicesRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        Integer year = request != null ? request.year() : null;
        Integer month = request != null ? request.month() : null;
        return monthlyInvoiceService.generateAndEmailAllOwners(year, month, session.role());
    }

    @PostMapping("/{invoiceId}/mark-paid")
    public void markInvoicePaid(@PathVariable Long invoiceId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        monthlyInvoiceService.markInvoicePaid(invoiceId, session.role());
    }
}
