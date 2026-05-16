package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Integer id, 
            @RequestBody UserRoleUpdateDTO request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreateDTO request) {
        // Recibe el JSON de Angular, se lo pasa al servicio (que encripta la clave y asigna sede)
        UserResponseDTO nuevoUsuario = userService.createUser(request);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_GRAL', 'ADMIN', 'GERENTE_GRAL')")
    public ResponseEntity<Void> activateUser(@PathVariable Integer id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

}