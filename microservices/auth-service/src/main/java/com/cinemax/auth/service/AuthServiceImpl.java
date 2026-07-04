package com.cinemax.auth.service;

import com.cinemax.auth.client.UsuariosClient;
import com.cinemax.auth.dto.LoginRequestDTO;
import com.cinemax.auth.dto.LoginResponseDTO;
import com.cinemax.auth.dto.ValidateCredentialsRequestDTO;
import com.cinemax.auth.dto.ValidateCredentialsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuariosClient usuariosClient;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        ValidateCredentialsResponseDTO validation;

        try {
            validation = usuariosClient.validateCredentials(
                    new ValidateCredentialsRequestDTO(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo contactar al servicio de usuarios. Intenta más tarde.", e);
        }

        if (!validation.isValid()) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        String token = jwtService.generateToken(
                validation.getEmail(),
                validation.getRole(),
                validation.getFirstName(),
                validation.getIdVenue(),
                validation.getPermissions()
        );

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }
}