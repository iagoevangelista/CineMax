package com.cinemax.backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cinemax.backend.model.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
