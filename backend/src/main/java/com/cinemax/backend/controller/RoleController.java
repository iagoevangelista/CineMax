package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.role.RoleResponseDTO;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.util.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public List<RoleResponseDTO> getRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> !RoleConstants.CLIENTE.equals(r.getRoleName()))
                .map(r -> new RoleResponseDTO(r.getIdRole(), r.getRoleName()))
                .toList();
    }
}