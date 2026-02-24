package com.animalfarm.repository;

import com.animalfarm.model.InvoiceParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceParameterRepository extends JpaRepository<InvoiceParameter, Long> {
}
