package com.animalfarm.service;

import com.animalfarm.dto.AnimalSummary;
import com.animalfarm.model.AnimalType;
import com.animalfarm.model.Owner;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final AnimalService animalService;
    private final OwnerService ownerService;

    public ReportService(AnimalService animalService, OwnerService ownerService) {
        this.animalService = animalService;
        this.ownerService = ownerService;
    }

    public byte[] ownerVsAnimalPdf(UUID ownerId) {
        var owner = ownerService.getOwner(ownerId);
        var animals = animalService.getByOwner(ownerId);
        String title = "Owner vs Animal Report";
        String subtitle = "Owner: " + owner.getFirstName() + " " + owner.getLastName() + " (" + owner.getEmail() + ")";
        return buildPdf(title, subtitle, animals);
    }

    public byte[] parentVsAnimalPdf(UUID parentId) {
        var animals = animalService.getByParent(parentId);
        String title = "Parent vs Animal Report";
        String subtitle = "Parent Animal UUID: " + parentId;
        return buildPdf(title, subtitle, animals);
    }

    public byte[] ownerAnimalReportPdf(UUID ownerId) {
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
                        "OwnerId: " + owner.getOwnerId()
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

    public byte[] ownerAnimalTypeCountsPdf() {
        var owners = ownerService.listOwners();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Owner Animal Type Counts Report"));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.addCell(headerCell("owner_id"));
            table.addCell(headerCell("firstname"));
            table.addCell(headerCell("Cattle"));
            table.addCell(headerCell("Goats"));
            table.addCell(headerCell("Rams"));
            table.addCell(headerCell("Pigs"));

            for (Owner owner : owners) {
                List<AnimalSummary> animals = animalService.getByOwner(owner.getOwnerId());
                Map<AnimalType, Long> counts = animals.stream()
                        .collect(Collectors.groupingBy(AnimalSummary::type, Collectors.counting()));
                long cattle = counts.getOrDefault(AnimalType.CATTLE, 0L);
                long goats = counts.getOrDefault(AnimalType.GOAT, 0L);
                long rams = counts.getOrDefault(AnimalType.RAM, 0L);
                long pigs = counts.getOrDefault(AnimalType.PIG, 0L);
                table.addCell(String.valueOf(owner.getOwnerId()));
                table.addCell(owner.getFirstName());
                table.addCell(String.valueOf(cattle));
                table.addCell(String.valueOf(goats));
                table.addCell(String.valueOf(rams));
                table.addCell(String.valueOf(pigs));
            }
            if (owners.isEmpty()) {
                PdfPCell noData = new PdfPCell(new Phrase("No owners found."));
                noData.setColspan(6);
                table.addCell(noData);
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate owner animal type counts PDF", e);
        }
    }

    private PdfPCell headerCell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setPadding(6f);
        return cell;
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
