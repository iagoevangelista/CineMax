package com.cinemax.usuariosservice.controller;

import com.cinemax.usuariosservice.model.dto.user.*;
import com.cinemax.usuariosservice.model.dto.venue.VenueDTO;
import com.cinemax.usuariosservice.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Panel Administrativo ---

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Integer id, @Valid @RequestBody UserRoleUpdateDTO request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Integer id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado exitosamente"));
    }

    // --- Perfil (Autogestión) ---

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getMyProfile(principal.getName()));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestPart("user") UserUpdateDTO updateDTO,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(updateDTO, image, principal.getName()));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Principal principal) {
        userService.deleteMyAccount(principal.getName());
        return ResponseEntity.noContent().build();
    }

    // --- Hallazgo 2 (Combinación de datos con Sucursales) ---

    @GetMapping("/venues/available-for-role/{idRole}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<List<VenueDTO>> getAvailableVenuesForRole(@PathVariable Integer idRole) {
        return ResponseEntity.ok(userService.getAvailableVenuesForRole(idRole));
    }
}