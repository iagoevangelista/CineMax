package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Integer> {

    List<Venue> findByStatus(String status);
}