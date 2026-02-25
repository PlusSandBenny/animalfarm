package com.animalfarm.repository;

import com.animalfarm.model.OwnerInvoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerInvoiceRepository extends JpaRepository<OwnerInvoice, Long> {
    Optional<OwnerInvoice> findByOwnerIdAndPeriodYearAndPeriodMonth(Long ownerId, Integer periodYear, Integer periodMonth);
    List<OwnerInvoice> findByOwnerIdAndPaidFalseOrderByPeriodYearDescPeriodMonthDesc(Long ownerId);
    List<OwnerInvoice> findAllByOrderByCreatedAtDesc();
    List<OwnerInvoice> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<OwnerInvoice> findByPeriodYearAndPeriodMonthOrderByCreatedAtDesc(Integer periodYear, Integer periodMonth);
    List<OwnerInvoice> findByOwnerIdAndPeriodYearAndPeriodMonthOrderByCreatedAtDesc(Long ownerId, Integer periodYear, Integer periodMonth);
}
