package com.animalfarm.service;

import com.animalfarm.model.OwnerInvoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class InvoiceEmailService {
    private final JavaMailSender javaMailSender;
    private final InvoicePdfService invoicePdfService;
    private final String fromEmail;
    private final String mailHost;
    private final int mailPort;
    private final String mailUsername;
    private final boolean smtpAuth;
    private final boolean smtpStartTls;

    public InvoiceEmailService(
            JavaMailSender javaMailSender,
            InvoicePdfService invoicePdfService,
            @Value("${app.invoice.from-email}") String fromEmail,
            @Value("${spring.mail.host}") String mailHost,
            @Value("${spring.mail.port}") int mailPort,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean smtpStartTls
    ) {
        this.javaMailSender = javaMailSender;
        this.invoicePdfService = invoicePdfService;
        this.fromEmail = fromEmail;
        this.mailHost = mailHost;
        this.mailPort = mailPort;
        this.mailUsername = mailUsername;
        this.smtpAuth = smtpAuth;
        this.smtpStartTls = smtpStartTls;
    }

    public void sendOwnerInvoice(OwnerInvoice invoice, String smtpPassword) {
        byte[] pdf = invoicePdfService.buildInvoicePdf(invoice);
        try {
            JavaMailSender sender = createSenderForRequest(smtpPassword);
            var message = sender.createMimeMessage();
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
                    "invoice-" + invoice.getOwner().getOwnerId() + "-" + invoice.getPeriodYear() + "-" + invoice.getPeriodMonth() + ".pdf",
                    new ByteArrayResource(pdf)
            );
            sender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    private JavaMailSender createSenderForRequest(String smtpPassword) {
        if (smtpPassword == null || smtpPassword.isBlank()) {
            return javaMailSender;
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailHost);
        sender.setPort(mailPort);
        sender.setUsername(mailUsername);
        sender.setPassword(smtpPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtpStartTls));
        return sender;
    }
}
