package com.cinemax.usuariosservice.controller;

import com.cinemax.usuariosservice.model.dto.auth.ForgotPasswordRequestDTO;
import com.cinemax.usuariosservice.model.dto.auth.ResetPasswordRequestDTO;
import com.cinemax.usuariosservice.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerClient(@Valid @RequestBody com.cinemax.usuariosservice.model.dto.auth.UserRegisterDTO request) {
        userService.registerClient(request);
        return ResponseEntity.ok(Map.of("message", "Cliente registrado exitosamente."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@PathVariable String token) {
        return ResponseEntity.ok(Map.of("valid", userService.validateResetToken(token)));
    }

    // Compatibilidad con el contrato del monolito (GET /api/v1/auth/validate-token?token=xxx),
    // que el frontend Angular ya consume. Mantener ambas rutas evita tocar el cliente.
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateTokenLegacy(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("isValid", userService.validateResetToken(token)));
    }
}