package com.animalfarm.service;

import com.animalfarm.dto.GeneratedInvoiceSummary;
import com.animalfarm.dto.InvoiceHistoryResponse;
import com.animalfarm.dto.MonthlyInvoiceResponse;
import com.animalfarm.exception.ApiException;
import com.animalfarm.model.ActorRole;
import com.animalfarm.model.AnimalType;
import com.animalfarm.model.InvoiceParameter;
import com.animalfarm.model.Owner;
import com.animalfarm.model.OwnerInvoice;
import com.animalfarm.repository.AnimalRepository;
import com.animalfarm.repository.OwnerInvoiceRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyInvoiceService {
    private final AnimalRepository animalRepository;
    private final OwnerService ownerService;
    private final InvoiceParameterService invoiceParameterService;
    private final OwnerInvoiceRepository ownerInvoiceRepository;
    private final InvoiceEmailService invoiceEmailService;
    private final InvoicePdfService invoicePdfService;

    public MonthlyInvoiceService(
            AnimalRepository animalRepository,
            OwnerService ownerService,
            InvoiceParameterService invoiceParameterService,
            OwnerInvoiceRepository ownerInvoiceRepository,
            InvoiceEmailService invoiceEmailService,
            InvoicePdfService invoicePdfService
    ) {
        this.animalRepository = animalRepository;
        this.ownerService = ownerService;
        this.invoiceParameterService = invoiceParameterService;
        this.ownerInvoiceRepository = ownerInvoiceRepository;
        this.invoiceEmailService = invoiceEmailService;
        this.invoicePdfService = invoicePdfService;
    }

    public MonthlyInvoiceResponse generateForOwner(Long ownerId, ActorRole role, Long requesterOwnerId) {
        if (role == ActorRole.OWNER && (requesterOwnerId == null || !requesterOwnerId.equals(ownerId))) {
            throw new ApiException("Owner can only generate own monthly invoice.");
        }
        var owner = ownerService.getOwner(ownerId);
        InvoiceParameter p = invoiceParameterService.getInternal();
        return calculate(owner, p);
    }

    public List<MonthlyInvoiceResponse> generateForAllOwners(ActorRole role) {
        RoleValidator.requireAdmin(role);
        InvoiceParameter p = invoiceParameterService.getInternal();
        return ownerService.listOwners().stream().map(owner -> calculate(owner, p)).toList();
    }

    @Transactional
    public List<GeneratedInvoiceSummary> generateAndEmailAllOwners(Integer year, Integer month, ActorRole role) {
        RoleValidator.requireAdmin(role);
        YearMonth ym = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        InvoiceParameter p = invoiceParameterService.getInternal();
        return ownerService.listOwners().stream()
                .map(owner -> generateSingle(owner, ym, p))
                .toList();
    }

    @Transactional
    public void markInvoicePaid(Long invoiceId, ActorRole role) {
        RoleValidator.requireAdmin(role);
        OwnerInvoice invoice = ownerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found: " + invoiceId));
        invoice.setPaid(true);
    }

    public List<InvoiceHistoryResponse> getInvoiceHistory(ActorRole role, Long requesterOwnerId, Long ownerId, Integer year, Integer month) {
        Long effectiveOwnerId = ownerId;
        if (role == ActorRole.OWNER) {
            effectiveOwnerId = requesterOwnerId;
        }

        List<OwnerInvoice> invoices;
        if (effectiveOwnerId != null && year != null && month != null) {
            invoices = ownerInvoiceRepository.findByOwnerIdAndPeriodYearAndPeriodMonthOrderByCreatedAtDesc(effectiveOwnerId, year, month);
        } else if (effectiveOwnerId != null) {
            invoices = ownerInvoiceRepository.findByOwnerIdOrderByCreatedAtDesc(effectiveOwnerId);
        } else if (year != null && month != null) {
            RoleValidator.requireAdmin(role);
            invoices = ownerInvoiceRepository.findByPeriodYearAndPeriodMonthOrderByCreatedAtDesc(year, month);
        } else {
            RoleValidator.requireAdmin(role);
            invoices = ownerInvoiceRepository.findAllByOrderByCreatedAtDesc();
        }
        return invoices.stream().map(this::toHistory).toList();
    }

    public byte[] downloadInvoicePdf(Long invoiceId, ActorRole role, Long requesterOwnerId) {
        OwnerInvoice invoice = ownerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("Invoice not found: " + invoiceId));
        if (role == ActorRole.OWNER && (requesterOwnerId == null || !requesterOwnerId.equals(invoice.getOwner().getId()))) {
            throw new ApiException("Owner can only download own invoice.");
        }
        return invoicePdfService.buildInvoicePdf(invoice);
    }

    public byte[] downloadInvoicesZip(ActorRole role, Long requesterOwnerId, Long ownerId, Integer year, Integer month) {
        List<OwnerInvoice> invoices = selectInvoices(role, requesterOwnerId, ownerId, year, month);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(out)) {
            for (OwnerInvoice invoice : invoices) {
                String name = "invoice-" + invoice.getId() + "-owner-" + invoice.getOwner().getId()
                        + "-" + invoice.getPeriodYear() + "-" + String.format("%02d", invoice.getPeriodMonth()) + ".pdf";
                zip.putNextEntry(new ZipEntry(name));
                zip.write(invoicePdfService.buildInvoicePdf(invoice));
                zip.closeEntry();
            }
            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate invoice zip", e);
        }
    }

    private GeneratedInvoiceSummary generateSingle(Owner owner, YearMonth ym, InvoiceParameter p) {
        OwnerInvoice invoice = ownerInvoiceRepository
                .findByOwnerIdAndPeriodYearAndPeriodMonth(owner.getId(), ym.getYear(), ym.getMonthValue())
                .orElseGet(() -> createInvoice(owner, ym, p));

        if (!invoice.isEmailSent()) {
            try {
                invoiceEmailService.sendOwnerInvoice(invoice);
                invoice.setEmailSent(true);
                invoice.setEmailError(null);
                invoice.setSentAt(LocalDateTime.now());
            } catch (RuntimeException e) {
                invoice.setEmailSent(false);
                invoice.setEmailError(e.getMessage());
            }
        }
        return toSummary(invoice);
    }

    private OwnerInvoice createInvoice(Owner owner, YearMonth ym, InvoiceParameter p) {
        long cattle = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.CATTLE);
        long goats = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.GOAT);
        long rams = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.RAM);
        long pigs = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.PIG);

        BigDecimal cattleRate = p.getCattleMonthlyFeeds().add(p.getCattleMonthlyMedication());
        BigDecimal goatRate = p.getGoatMonthlyFeeds().add(p.getGoatMonthlyMedication());
        BigDecimal ramRate = p.getRamMonthlyFeeds().add(p.getRamMonthlyMedication());
        BigDecimal pigRate = p.getPigMonthlyFeeds().add(p.getPigMonthlyMedication());

        BigDecimal currentCharge = cattleRate.multiply(BigDecimal.valueOf(cattle))
                .add(goatRate.multiply(BigDecimal.valueOf(goats)))
                .add(ramRate.multiply(BigDecimal.valueOf(rams)))
                .add(pigRate.multiply(BigDecimal.valueOf(pigs)));

        BigDecimal previousUnpaid = latestUnpaidBalanceBefore(owner.getId(), ym);
        BigDecimal totalDue = currentCharge.add(previousUnpaid);

        OwnerInvoice invoice = new OwnerInvoice();
        invoice.setOwner(owner);
        invoice.setPeriodYear(ym.getYear());
        invoice.setPeriodMonth(ym.getMonthValue());
        invoice.setCattleCount(cattle);
        invoice.setGoatCount(goats);
        invoice.setRamCount(rams);
        invoice.setPigCount(pigs);
        invoice.setCurrentCharge(currentCharge);
        invoice.setPreviousUnpaidBalance(previousUnpaid);
        invoice.setTotalDue(totalDue);
        invoice.setPaid(false);
        invoice.setEmailSent(false);
        return ownerInvoiceRepository.save(invoice);
    }

    private BigDecimal latestUnpaidBalanceBefore(Long ownerId, YearMonth ym) {
        return ownerInvoiceRepository.findByOwnerIdAndPaidFalseOrderByPeriodYearDescPeriodMonthDesc(ownerId).stream()
                .filter(inv -> YearMonth.of(inv.getPeriodYear(), inv.getPeriodMonth()).isBefore(ym))
                .findFirst()
                .map(OwnerInvoice::getTotalDue)
                .orElse(BigDecimal.ZERO);
    }

    private MonthlyInvoiceResponse calculate(Owner owner, InvoiceParameter p) {
        long cattle = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.CATTLE);
        long goats = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.GOAT);
        long rams = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.RAM);
        long pigs = animalRepository.countByOwnerIdAndTypeAndSoldFalse(owner.getId(), AnimalType.PIG);

        BigDecimal cattleRate = p.getCattleMonthlyFeeds().add(p.getCattleMonthlyMedication());
        BigDecimal goatRate = p.getGoatMonthlyFeeds().add(p.getGoatMonthlyMedication());
        BigDecimal ramRate = p.getRamMonthlyFeeds().add(p.getRamMonthlyMedication());
        BigDecimal pigRate = p.getPigMonthlyFeeds().add(p.getPigMonthlyMedication());

        BigDecimal total = cattleRate.multiply(BigDecimal.valueOf(cattle))
                .add(goatRate.multiply(BigDecimal.valueOf(goats)))
                .add(ramRate.multiply(BigDecimal.valueOf(rams)))
                .add(pigRate.multiply(BigDecimal.valueOf(pigs)));

        return new MonthlyInvoiceResponse(
                owner.getId(),
                owner.getFirstName(),
                cattle,
                goats,
                rams,
                pigs,
                total
        );
    }

    private GeneratedInvoiceSummary toSummary(OwnerInvoice invoice) {
        return new GeneratedInvoiceSummary(
                invoice.getId(),
                invoice.getOwner().getId(),
                invoice.getOwner().getFirstName(),
                invoice.getOwner().getEmail(),
                invoice.getPeriodYear(),
                invoice.getPeriodMonth(),
                invoice.getCurrentCharge(),
                invoice.getPreviousUnpaidBalance(),
                invoice.getTotalDue(),
                invoice.isPaid(),
                invoice.isEmailSent(),
                invoice.getEmailError()
        );
    }

    private InvoiceHistoryResponse toHistory(OwnerInvoice invoice) {
        return new InvoiceHistoryResponse(
                invoice.getId(),
                invoice.getOwner().getId(),
                invoice.getOwner().getFirstName(),
                invoice.getOwner().getEmail(),
                invoice.getPeriodYear(),
                invoice.getPeriodMonth(),
                invoice.getCurrentCharge(),
                invoice.getPreviousUnpaidBalance(),
                invoice.getTotalDue(),
                invoice.isPaid(),
                invoice.isEmailSent(),
                invoice.getEmailError(),
                invoice.getCreatedAt()
        );
    }

    private List<OwnerInvoice> selectInvoices(ActorRole role, Long requesterOwnerId, Long ownerId, Integer year, Integer month) {
        Long effectiveOwnerId = ownerId;
        if (role == ActorRole.OWNER) {
            effectiveOwnerId = requesterOwnerId;
        }

        if (effectiveOwnerId != null && year != null && month != null) {
            return ownerInvoiceRepository.findByOwnerIdAndPeriodYearAndPeriodMonthOrderByCreatedAtDesc(effectiveOwnerId, year, month);
        }
        if (effectiveOwnerId != null) {
            return ownerInvoiceRepository.findByOwnerIdOrderByCreatedAtDesc(effectiveOwnerId);
        }
        if (year != null && month != null) {
            RoleValidator.requireAdmin(role);
            return ownerInvoiceRepository.findByPeriodYearAndPeriodMonthOrderByCreatedAtDesc(year, month);
        }
        RoleValidator.requireAdmin(role);
        return ownerInvoiceRepository.findAllByOrderByCreatedAtDesc();
    }
}
