package com.cinemax.backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cinemax.backend.model.entity.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}
