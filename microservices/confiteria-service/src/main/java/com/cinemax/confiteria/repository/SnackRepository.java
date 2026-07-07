package com.cinemax.confiteria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinemax.confiteria.entity.Snack;

public interface SnackRepository extends JpaRepository<Snack, Integer> {

    boolean existsByNameSnack(String nameSnack);

    List<Snack> findByStatus(String status);

    List<Snack> findBySnackCategory_IdSnackCategory(Integer idSnackCategory);

    List<Snack> findByIdVenue(Integer idVenue);
}