package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.location.DepartmentResponseDTO;
import com.cinemax.backend.model.dto.location.DistrictResponseDTO;
import com.cinemax.backend.model.dto.location.ProvinceResponseDTO;
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
    public ResponseEntity<List<DepartmentResponseDTO>> getDepartments() {
        var result = departmentRepository.findAll().stream()
                .map(d -> new DepartmentResponseDTO(d.getIdDepartment(), d.getNameDepartment()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/provinces/{idDepartment}")
    public ResponseEntity<List<ProvinceResponseDTO>> getProvincesByDepartment(@PathVariable Integer idDepartment) {
        var result = provinceRepository.findByDepartment_IdDepartment(idDepartment).stream()
                .map(p -> new ProvinceResponseDTO(p.getIdProvince(), p.getNameProvince(), p.getDepartment().getIdDepartment()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/districts/{idProvince}")
    public ResponseEntity<List<DistrictResponseDTO>> getDistrictsByProvince(@PathVariable Integer idProvince) {
        var result = districtRepository.findByProvince_IdProvince(idProvince).stream()
                .map(d -> new DistrictResponseDTO(d.getIdDistrict(), d.getNameDistrict(), d.getProvince().getIdProvince()))
                .toList();
        return ResponseEntity.ok(result);
    }
}