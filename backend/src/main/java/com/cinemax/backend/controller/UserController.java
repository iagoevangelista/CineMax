package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.user.UserCreateDTO;
import com.cinemax.backend.model.dto.user.UserResponseDTO;
import com.cinemax.backend.model.dto.user.UserRoleUpdateDTO;
import com.cinemax.backend.model.dto.user.UserUpdateDTO;
import com.cinemax.backend.service.user.UserService;

import jakarta.validation.Valid; // <-- Reincorporado
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map; // <-- Reincorporado

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
            @Valid @RequestBody UserRoleUpdateDTO request) { // <-- Aplicando @Valid
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
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO request) { // <-- Aplicando @Valid
        // Recibe el JSON de Angular, se lo pasa al servicio (que encripta la clave y asigna sede)
        UserResponseDTO nuevoUsuario = userService.createUser(request);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GERENTE_GENERAL', 'ADMIN', 'GERENTE_GENERAL')")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Integer id) { 
        // <-- Ejemplo de uso de Map para devolver un JSON simple: {"message": "Usuario activado"}
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado exitosamente"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getMyProfile(Principal principal) {
        // principal.getName() obtiene directamente el email del token inyectado
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestPart("user") UserUpdateDTO updateDTO, // <-- Aplicando @Valid
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) { 
        
        String email = principal.getName(); 
        return ResponseEntity.ok(userService.updateProfile(updateDTO, image, email));
    }
}