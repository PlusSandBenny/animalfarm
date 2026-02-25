package com.animalfarm.service;

import com.animalfarm.model.OwnerInvoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailService {
    private final JavaMailSender javaMailSender;
    private final InvoicePdfService invoicePdfService;
    private final String fromEmail;

    public InvoiceEmailService(
            JavaMailSender javaMailSender,
            InvoicePdfService invoicePdfService,
            @Value("${app.invoice.from-email}") String fromEmail
    ) {
        this.javaMailSender = javaMailSender;
        this.invoicePdfService = invoicePdfService;
        this.fromEmail = fromEmail;
    }

    public void sendOwnerInvoice(OwnerInvoice invoice) {
        byte[] pdf = invoicePdfService.buildInvoicePdf(invoice);
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
}
