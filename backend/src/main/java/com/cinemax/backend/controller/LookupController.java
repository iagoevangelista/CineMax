package com.cinemax.backend.controller;

import com.cinemax.backend.model.entity.Department;
import com.cinemax.backend.model.entity.District;
import com.cinemax.backend.model.entity.Province;
import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.repository.DepartmentRepository;
import com.cinemax.backend.repository.DistrictRepository;
import com.cinemax.backend.repository.ProvinceRepository;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.util.RoleConstants;
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
        return roleRepository.findAll().stream()
                .filter(r -> !RoleConstants.CLIENTE.equals(r.getRoleName()))
                .toList();
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
