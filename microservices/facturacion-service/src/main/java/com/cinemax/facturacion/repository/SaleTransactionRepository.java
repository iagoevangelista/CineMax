package com.cinemax.facturacion.repository;

import com.cinemax.facturacion.model.entity.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Integer> {
    List<SaleTransaction> findByIdUserOrderByPaymentDateDesc(Integer idUser);
}