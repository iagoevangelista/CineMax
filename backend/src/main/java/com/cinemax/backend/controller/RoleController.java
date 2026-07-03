package com.cinemax.backend.controller;

import com.cinemax.backend.model.entity.Role;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.util.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<Role> getRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> !RoleConstants.CLIENTE.equals(r.getRoleName()))
                .toList();
    }
}
