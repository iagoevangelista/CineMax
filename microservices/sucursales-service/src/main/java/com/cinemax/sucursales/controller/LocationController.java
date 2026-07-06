package com.cinemax.sucursales.controller;

import com.cinemax.sucursales.dto.DepartmentResponseDTO;
import com.cinemax.sucursales.dto.DistrictResponseDTO;
import com.cinemax.sucursales.dto.ProvinceResponseDTO;
import com.cinemax.sucursales.repository.DepartmentRepository;
import com.cinemax.sucursales.repository.DistrictRepository;
import com.cinemax.sucursales.repository.ProvinceRepository;
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