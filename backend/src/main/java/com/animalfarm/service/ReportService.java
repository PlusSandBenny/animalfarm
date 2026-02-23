package com.animalfarm.service;

import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.model.Owner;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final AnimalService animalService;
    private final OwnerService ownerService;

    public ReportService(AnimalService animalService, OwnerService ownerService) {
        this.animalService = animalService;
        this.ownerService = ownerService;
    }

    public byte[] ownerVsAnimalPdf(Long ownerId) {
        var owner = ownerService.getOwner(ownerId);
        var animals = animalService.getByOwner(ownerId);
        String title = "Owner vs Animal Report";
        String subtitle = "Owner: " + owner.getFirstName() + " " + owner.getLastName() + " (" + owner.getEmail() + ")";
        return buildPdf(title, subtitle, animals);
    }

    public byte[] parentVsAnimalPdf(Long parentId) {
        var animals = animalService.getByParent(parentId);
        String title = "Parent vs Animal Report";
        String subtitle = "Parent Animal DB Id: " + parentId;
        return buildPdf(title, subtitle, animals);
    }

    public byte[] ownerAnimalReportPdf(Long ownerId) {
        var owner = ownerService.getOwner(ownerId);
        var animals = animalService.getByOwner(ownerId);
        String title = "Owner Animal Report";
        String subtitle = "Owner: " + owner.getFirstName() + " " + owner.getLastName();
        return buildPdf(title, subtitle, animals);
    }

    public byte[] ownersListPdf() {
        var owners = ownerService.listOwners();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Owners List Report"));
            document.add(new Paragraph("Total owners: " + owners.size()));
            document.add(new Paragraph(" "));
            for (Owner owner : owners) {
                document.add(new Paragraph(
                        "OwnerId: " + owner.getId()
                                + ", Name: " + owner.getFirstName() + " " + owner.getLastName()
                                + ", Email: " + owner.getEmail()
                                + ", Phone: " + owner.getPhoneNumber()
                                + ", Address: " + owner.getAddress()
                ));
            }
            if (owners.isEmpty()) {
                document.add(new Paragraph("No owners found."));
            }
            document.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate owners list PDF", e);
        }
    }

    private byte[] buildPdf(String title, String subtitle, List<AnimalSummary> animals) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph(subtitle));
            document.add(new Paragraph(" "));
            for (AnimalSummary animal : animals) {
                document.add(new Paragraph(
                        "AnimalId: " + animal.animalId()
                                + ", Type: " + animal.type()
                                + ", Breed: " + animal.breed()
                                + ", Color: " + animal.color()
                                + ", DOB: " + animal.dateOfBirth()
                                + ", Sold: " + animal.sold()
                ));
            }
            if (animals.isEmpty()) {
                document.add(new Paragraph("No records found."));
            }
            document.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
