package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;
import com.cinemax.backend.service.auth.AuthService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    // Fíjate cómo inyectamos la INTERFAZ, no la clase de implementación
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        // Llamamos al método de tu AuthService (asegúrate de que se llame así, 
        // o cámbialo por el nombre exacto que le pusiste en tu AuthService.java)
        authService.requestPasswordReset(email); 
        
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, se ha enviado un enlace de recuperación."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        // Llamamos al método de tu AuthService para cambiar la clave
        authService.resetPassword(token, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida con éxito."));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("isValid", isValid));
    }

}