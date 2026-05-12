package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvince_IdProvince(Integer idProvince);
}