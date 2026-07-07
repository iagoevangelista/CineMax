package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Integer> {

    List<District> findByProvince_IdProvince(Integer idProvince);
}