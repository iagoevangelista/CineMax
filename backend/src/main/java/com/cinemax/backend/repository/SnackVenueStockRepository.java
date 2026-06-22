package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SnackVenueStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnackVenueStockRepository extends JpaRepository<SnackVenueStock, Integer> {
    List<SnackVenueStock> findByVenue_IdVenueAndStockGreaterThan(Integer idVenue, Integer stock);
    List<SnackVenueStock> findByVenue_IdVenue(Integer idVenue);
    Optional<SnackVenueStock> findBySnack_IdSnackAndVenue_IdVenue(Integer idSnack, Integer idVenue);
}