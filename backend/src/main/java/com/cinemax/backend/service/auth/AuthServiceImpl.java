package com.cinemax.backend.service.auth;

import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;
import java.util.HashMap;
import java.util.Map;
import com.cinemax.backend.model.entity.DocumentType;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import com.cinemax.backend.security.JwtService;
import com.cinemax.backend.repository.DocumentTypeRepository;
import com.cinemax.backend.service.email.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService; // <-- Movido aquí arriba de forma limpia
    private final DocumentTypeRepository documentTypeRepository; // NUEVO: Inyectamos el repo

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserAccount user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName()); 

        String jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponseDTO.builder().token(jwtToken).build();
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {

        // --- 1. VALIDAR UNICIDAD DEL CORREO ---
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrónico ya se encuentra registrado.");
        }

        // 2. Buscamos el rol en la BD
        var role = roleRepository.findById(4)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

        // 3. Buscamos el tipo de documento REAL en la base de datos
        DocumentType docType = documentTypeRepository.findById(request.getIdDocumentType())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no válido."));

        // 4. Creamos el usuario
        UserAccount user = new UserAccount();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("Activo");
        user.setDocumentNumber(request.getDocumentNumber());
        user.setDocumentType(docType);

        // 5. Guardamos
        userRepository.save(user);

        // --- 6. ENVÍO DE CORREO DE BIENVENIDA ASÍNCRONO ---
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
            } catch (Exception e) {
                System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
            }
        });

        // 7. Generamos Token
        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder().token(jwtToken).build();
    }

    @Override
    public void requestPasswordReset(String email) {
        java.util.Optional<UserAccount> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            System.out.println("Intento de recuperación para correo inexistente: " + email);
            // Responde a Angular en 0.1 segundos
            return; 
        }

        UserAccount user = userOptional.get();
        String token = java.util.UUID.randomUUID().toString();
        
        user.setResetToken(token);
        user.setTokenExpiryDate(java.time.LocalDateTime.now().plusMinutes(1));
        userRepository.save(user);

        // LA MAGIA ASÍNCRONA: Mandamos el correo en un proceso en segundo plano (background thread)
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            } catch (Exception e) {
                System.err.println("Error al enviar el correo en segundo plano: " + e.getMessage());
            }
        });
        
        // El hilo principal no espera a Google. Responde a Angular inmediatamente en 0.1 segundos.
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Buscamos al usuario por el token secreto
        UserAccount user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no existe."));

        // Verificamos si ya pasaron los 15 minutos
        if (user.getTokenExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El enlace de recuperación ha expirado. Por favor solicita uno nuevo.");
        }

        // Encriptamos la nueva contraseña (corregido a setPasswordHash)
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        // Limpiamos el token para que no se pueda reciclar
        user.setResetToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);
    }

    @Override
    public boolean validateResetToken(String token) {
        return userRepository.findByResetToken(token)
                .map(user -> user.getTokenExpiryDate() != null && user.getTokenExpiryDate().isAfter(java.time.LocalDateTime.now()))
                .orElse(false);
    }

}