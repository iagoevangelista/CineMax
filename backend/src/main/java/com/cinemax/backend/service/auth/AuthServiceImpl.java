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
        // 1. Buscamos el rol en la BD
        var role = roleRepository.findById(4)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

        // 2. NUEVO: Buscamos el tipo de documento REAL en la base de datos
        DocumentType docType = documentTypeRepository.findById(request.getIdDocumentType())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no válido."));

        // 3. Creamos el usuario
        UserAccount user = new UserAccount();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("Activo");
        user.setDocumentNumber(request.getDocumentNumber());

        // 4. Asignamos la entidad real que trajimos de la BD
        user.setDocumentType(docType);

        // 5. Guardamos
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder().token(jwtToken).build();
    }
}