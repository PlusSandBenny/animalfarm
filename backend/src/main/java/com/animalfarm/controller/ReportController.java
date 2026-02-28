package com.animalfarm.controller;

import com.animalfarm.auth.AuthContext;
import com.animalfarm.auth.AuthSession;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/owner-vs-animal")
    public ResponseEntity<byte[]> ownerVsAnimal(@RequestParam UUID ownerId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() != ActorRole.ADMIN) {
            throw new ApiException("Only admin can generate this report.");
        }
        return pdf("owner-vs-animal-" + ownerId + ".pdf", reportService.ownerVsAnimalPdf(ownerId));
    }

    @GetMapping("/parent-vs-animal")
    public ResponseEntity<byte[]> parentVsAnimal(@RequestParam UUID parentId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() != ActorRole.ADMIN) {
            throw new ApiException("Only admin can generate this report.");
        }
        return pdf("parent-vs-animal-" + parentId + ".pdf", reportService.parentVsAnimalPdf(parentId));
    }

    @GetMapping("/owners")
    public ResponseEntity<byte[]> ownersList(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() != ActorRole.ADMIN) {
            throw new ApiException("Only admin can generate this report.");
        }
        return pdf("owners-list.pdf", reportService.ownersListPdf());
    }

    @GetMapping("/owners-animal-type-counts")
    public ResponseEntity<byte[]> ownersAnimalTypeCounts(HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() != ActorRole.ADMIN) {
            throw new ApiException("Only admin can generate this report.");
        }
        return pdf("owners-animal-type-counts.pdf", reportService.ownerAnimalTypeCountsPdf());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<byte[]> ownerReport(@PathVariable UUID ownerId, HttpServletRequest httpRequest) {
        AuthSession session = AuthContext.require(httpRequest);
        if (session.role() == ActorRole.OWNER && (session.ownerId() == null || !session.ownerId().equals(ownerId))) {
            throw new ApiException("Owner can only generate own report.");
        }
        return pdf("owner-animal-" + ownerId + ".pdf", reportService.ownerAnimalReportPdf(ownerId));
    }

    private ResponseEntity<byte[]> pdf(String filename, byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
