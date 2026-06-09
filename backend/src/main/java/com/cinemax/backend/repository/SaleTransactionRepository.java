package com.cinemax.backend.repository;


import com.cinemax.backend.model.entity.SaleTransaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Integer> {

}
