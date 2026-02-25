package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.dto.GeneratedInvoiceSummary;
import com.animalfarm.dto.GenerateMonthlyInvoicesRequest;
import com.animalfarm.dto.InvoiceHistoryResponse;
import com.animalfarm.dto.MonthlyInvoiceResponse;
import com.animalfarm.service.MonthlyInvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
        Integer year = request == null ? null : request.year();
        Integer month = request == null ? null : request.month();
        return monthlyInvoiceService.generateAndEmailAllOwners(year, month, session.role());
    }

    @PostMapping("/{invoiceId}/mark-paid")
    public ResponseEntity<Void> markPaid(@PathVariable Long invoiceId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        monthlyInvoiceService.markInvoicePaid(invoiceId, session.role());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public List<InvoiceHistoryResponse> history(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        return monthlyInvoiceService.getInvoiceHistory(session.role(), session.ownerId(), ownerId, year, month);
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long invoiceId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        byte[] pdf = monthlyInvoiceService.downloadInvoicePdf(invoiceId, session.role(), session.ownerId());
        String filename = "invoice-" + invoiceId + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/history/zip")
    public ResponseEntity<byte[]> downloadZip(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest httpRequest
    ) {
        AuthSession session = AuthContext.require(httpRequest);
        byte[] zip = monthlyInvoiceService.downloadInvoicesZip(session.role(), session.ownerId(), ownerId, year, month);
        String period = (year != null && month != null)
                ? (year + "-" + String.format("%02d", month))
                : YearMonth.now().toString();
        String filename = "invoices-" + period + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(zip);
    }
}
