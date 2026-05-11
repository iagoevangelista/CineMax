package com.cinemax.backend.controller;

import com.cinemax.backend.model.entity.*;
import com.cinemax.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LookupController {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @GetMapping("/roles")
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/provinces/{idDepartment}")
    public List<Province> getProvinces(@PathVariable Integer idDepartment) {
        return provinceRepository.findAll().stream()
                .filter(p -> p.getDepartment().getIdDepartment().equals(idDepartment))
                .toList();
    }

    @GetMapping("/districts/{idProvince}")
    public List<District> getDistricts(@PathVariable Integer idProvince) {
        return districtRepository.findAll().stream()
                .filter(d -> d.getProvince().getIdProvince().equals(idProvince))
                .toList();
    }
}