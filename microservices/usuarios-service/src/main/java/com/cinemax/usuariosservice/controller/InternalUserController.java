package com.cinemax.usuariosservice.controller;

import com.cinemax.usuariosservice.model.dto.auth.ValidateCredentialsRequestDTO;
import com.cinemax.usuariosservice.model.dto.auth.ValidateCredentialsResponseDTO;
import com.cinemax.usuariosservice.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping("/validate-credentials")
    public ResponseEntity<ValidateCredentialsResponseDTO> validateCredentials(
            @Valid @RequestBody ValidateCredentialsRequestDTO request) {
        return ResponseEntity.ok(userService.validateCredentials(request));
    }
}