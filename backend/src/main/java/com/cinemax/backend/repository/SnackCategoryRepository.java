package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SnackCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnackCategoryRepository extends JpaRepository<SnackCategory, Integer> {
}