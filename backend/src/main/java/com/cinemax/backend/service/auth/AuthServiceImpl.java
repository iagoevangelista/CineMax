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

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserAccount user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // NUEVO: Creamos un "extra claim" para meter el rol en el token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().getRoleName()); // Guardamos el nombre del rol (ADMIN, GERENTE, etc.)

        // Usamos el metodo de jwtService que acepta extraClaims
        String jwtToken = jwtService.generateToken(extraClaims, user);

        return AuthResponseDTO.builder().token(jwtToken).build();
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        var role = roleRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado. Asegúrate de insertarlo en la BD."));

        UserAccount user = new UserAccount();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("Activo");

        user.setDocumentNumber(request.getDocumentNumber());
        
        user.setDocumentType(DocumentType.builder().idDocumentType(request.getIdDocumentType()).build());

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder().token(jwtToken).build();
    }
}