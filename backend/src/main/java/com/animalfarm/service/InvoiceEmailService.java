package com.animalfarm.service;

import com.animalfarm.model.OwnerInvoice;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailService {
    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public InvoiceEmailService(
            JavaMailSender javaMailSender,
            @Value("${app.invoice.from-email}") String fromEmail
    ) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOwnerInvoice(OwnerInvoice invoice) {
        byte[] pdf = buildInvoicePdf(invoice);
        try {
            var message = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(invoice.getOwner().getEmail());
            helper.setSubject("Monthly Farm Invoice - " + invoice.getPeriodYear() + "-" + invoice.getPeriodMonth());
            helper.setText(
                    "Dear " + invoice.getOwner().getFirstName() + ",\n\n"
                            + "Attached is your monthly farm invoice.\n"
                            + "Total due: " + invoice.getTotalDue() + "\n\n"
                            + "Regards,\nAnimal Farm Admin"
            );
            helper.addAttachment(
                    "invoice-" + invoice.getOwner().getId() + "-" + invoice.getPeriodYear() + "-" + invoice.getPeriodMonth() + ".pdf",
                    new ByteArrayResource(pdf)
            );
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    private byte[] buildInvoicePdf(OwnerInvoice invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Owner Monthly Invoice"));
            document.add(new Paragraph("Owner ID: " + invoice.getOwner().getId()));
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
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
