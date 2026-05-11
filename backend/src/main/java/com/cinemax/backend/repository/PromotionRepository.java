package com.cinemax.backend.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cinemax.backend.model.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Integer> {
    List<Promotion> findByStatus(String status);
}
