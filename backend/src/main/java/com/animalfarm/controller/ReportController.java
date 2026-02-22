package com.animalfarm.controller;

import com.animalfarm.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/owner-vs-animal")
    public ResponseEntity<byte[]> ownerVsAnimal(@RequestParam Long ownerId) {
        return pdf("owner-vs-animal-" + ownerId + ".pdf", reportService.ownerVsAnimalPdf(ownerId));
    }

    @GetMapping("/parent-vs-animal")
    public ResponseEntity<byte[]> parentVsAnimal(@RequestParam Long parentId) {
        return pdf("parent-vs-animal-" + parentId + ".pdf", reportService.parentVsAnimalPdf(parentId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<byte[]> ownerReport(@PathVariable Long ownerId) {
        return pdf("owner-animal-" + ownerId + ".pdf", reportService.ownerAnimalReportPdf(ownerId));
    }

    private ResponseEntity<byte[]> pdf(String filename, byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
