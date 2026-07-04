package com.cinemax.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Lo que usuarios-service le responde a auth-service tras validar credenciales.
// Trae todo lo que auth-service necesita para armar el JWT (mismos campos
// que ya llevaba el token del monolito: role, firstName, idVenue, permissions).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateCredentialsResponseDTO {
    private boolean valid;
    private String email;
    private String role;
    private String firstName;
    private Integer idVenue;
    private List<String> permissions;
}