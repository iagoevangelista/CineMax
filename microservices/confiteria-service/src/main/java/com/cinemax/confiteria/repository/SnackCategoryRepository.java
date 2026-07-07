package com.cinemax.confiteria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinemax.confiteria.entity.SnackCategory;

public interface SnackCategoryRepository extends JpaRepository<SnackCategory, Integer> {

    boolean existsByNameCategory(String nameCategory);
}