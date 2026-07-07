package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {

    Optional<SeatType> findByNameSeatType(String nameSeatType);
}