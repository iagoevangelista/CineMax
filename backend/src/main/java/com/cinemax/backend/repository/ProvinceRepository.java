package com.cinemax.backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cinemax.backend.model.entity.Province;
import java.util.List;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    List<Province> findByDepartment_IdDepartment(Integer idDepartment);
}
