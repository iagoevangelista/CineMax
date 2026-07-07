package com.cinemax.usuariosservice.controller;

import com.cinemax.usuariosservice.model.dto.role.RoleResponseDTO;
import com.cinemax.usuariosservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        List<RoleResponseDTO> roles = roleRepository.findAll().stream()
                .filter(r -> !"CLIENTE".equals(r.getRoleName()))
                .map(r -> {
                    RoleResponseDTO dto = new RoleResponseDTO();
                    dto.setIdRole(r.getIdRole());
                    dto.setRoleName(r.getRoleName());
                    return dto;
                }).toList();
        return ResponseEntity.ok(roles);
    }
}