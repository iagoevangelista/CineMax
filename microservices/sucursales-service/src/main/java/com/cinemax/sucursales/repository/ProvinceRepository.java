package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, Integer> {

    List<Province> findByDepartment_IdDepartment(Integer idDepartment);
}