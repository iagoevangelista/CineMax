package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Integer> {
    List<SaleTransaction> findByUserAccount_IdUserOrderByPaymentDateDesc(Integer idUser);
}