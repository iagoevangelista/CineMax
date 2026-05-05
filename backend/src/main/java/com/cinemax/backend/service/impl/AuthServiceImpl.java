package com.cinemax.backend.service.impl;

import com.cinemax.backend.model.dto.auth.AuthRequestDTO;
import com.cinemax.backend.model.dto.auth.AuthResponseDTO;
import com.cinemax.backend.model.dto.auth.RegisterRequestDTO;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.repository.RoleRepository;
import com.cinemax.backend.repository.UserAccountRepository;
import com.cinemax.backend.security.JwtService;
import com.cinemax.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // --- DEPENDENCIAS ---
    private final UserAccountRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // --- MÉTODO 1: LOGIN ---
    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        // 1. El AuthenticationManager valida que el correo y la clave coincidan
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Buscamos al usuario en la BD de Azure
        UserAccount user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Generamos su JWT
        String jwtToken = jwtService.generateToken(user);

        // 4. Retornamos el DTO
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .build();
    }

    // --- MÉTODO 2: REGISTRO ---
    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // 1. Buscamos el rol 'ADMIN' (ID = 1) en Azure
        var role = roleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado. Asegúrate de insertarlo en la BD."));

        // 2. Construimos el nuevo usuario encriptando la clave con BCrypt
        UserAccount user = new UserAccount();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("Activo");

        // 3. Guardamos en la base de datos
        userRepository.save(user);

        // 4. Generamos y devolvemos su Token de acceso inmediato
        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}