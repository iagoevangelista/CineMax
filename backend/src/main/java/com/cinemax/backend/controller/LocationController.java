package com.cinemax.backend.controller;

import com.cinemax.backend.model.entity.Department;
import com.cinemax.backend.model.entity.District;
import com.cinemax.backend.model.entity.Province;
import com.cinemax.backend.repository.DepartmentRepository;
import com.cinemax.backend.repository.DistrictRepository;
import com.cinemax.backend.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final DepartmentRepository departmentRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/provinces/{idDepartment}")
    public ResponseEntity<List<Province>> getProvincesByDepartment(@PathVariable Integer idDepartment) {
        return ResponseEntity.ok(provinceRepository.findByDepartment_IdDepartment(idDepartment));
    }

    @GetMapping("/districts/{idProvince}")
    public ResponseEntity<List<District>> getDistrictsByProvince(@PathVariable Integer idProvince) {
        return ResponseEntity.ok(districtRepository.findByProvince_IdProvince(idProvince));
    }
}