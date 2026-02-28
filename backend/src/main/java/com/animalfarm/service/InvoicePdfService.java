package com.animalfarm.service;

import com.animalfarm.model.OwnerInvoice;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfService {
    public byte[] buildInvoicePdf(OwnerInvoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Owner Monthly Invoice"));
            document.add(new Paragraph("Invoice ID: " + invoice.getId()));
            document.add(new Paragraph("Owner ID: " + invoice.getOwner().getOwnerId()));
            document.add(new Paragraph("First Name: " + invoice.getOwner().getFirstName()));
            document.add(new Paragraph("Email: " + invoice.getOwner().getEmail()));
            document.add(new Paragraph("Period: " + invoice.getPeriodYear() + "-" + String.format("%02d", invoice.getPeriodMonth())));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Cattle Count: " + invoice.getCattleCount()));
            document.add(new Paragraph("Goat Count: " + invoice.getGoatCount()));
            document.add(new Paragraph("Ram Count: " + invoice.getRamCount()));
            document.add(new Paragraph("Pig Count: " + invoice.getPigCount()));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Current Charge: " + invoice.getCurrentCharge()));
            document.add(new Paragraph("Previous Unpaid Balance: " + invoice.getPreviousUnpaidBalance()));
            document.add(new Paragraph("Total Due: " + invoice.getTotalDue()));
            document.add(new Paragraph("Paid: " + invoice.isPaid()));
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
