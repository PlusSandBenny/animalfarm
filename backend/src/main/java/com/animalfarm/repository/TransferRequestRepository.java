package com.animalfarm.repository;

import com.animalfarm.model.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {
}
