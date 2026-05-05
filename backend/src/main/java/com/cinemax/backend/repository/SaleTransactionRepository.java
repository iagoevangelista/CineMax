package com.cinemax.backend.repository;


import com.cinemax.backend.model.entity.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Integer> {
}
