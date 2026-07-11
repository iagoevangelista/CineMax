package com.cinemax.facturacion.repository;

import com.cinemax.facturacion.model.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, Integer> {
    Optional<TransactionStatus> findByNameStatus(String nameStatus);
}