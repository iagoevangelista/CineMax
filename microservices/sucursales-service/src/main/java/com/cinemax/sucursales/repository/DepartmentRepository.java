package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}