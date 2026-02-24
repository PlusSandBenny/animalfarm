package com.animalfarm.service;

import com.animalfarm.dto.GeneratedInvoiceSummary;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyInvoiceService {
    private final AnimalRepository animalRepository;
    private final OwnerService ownerService;
    private final InvoiceParameterService invoiceParameterService;
    private final OwnerInvoiceRepository ownerInvoiceRepository;
    private final InvoiceEmailService invoiceEmailService;

    public MonthlyInvoiceService(
            AnimalRepository animalRepository,
            OwnerService ownerService,
            InvoiceParameterService invoiceParameterService,
            OwnerInvoiceRepository ownerInvoiceRepository,
            InvoiceEmailService invoiceEmailService
    ) {
        this.animalRepository = animalRepository;
        this.ownerService = ownerService;
        this.invoiceParameterService = invoiceParameterService;
        this.ownerInvoiceRepository = ownerInvoiceRepository;
        this.invoiceEmailService = invoiceEmailService;
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
}
