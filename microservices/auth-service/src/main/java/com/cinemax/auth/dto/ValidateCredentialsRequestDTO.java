package com.cinemax.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que auth-service le envía a usuarios-service para validar un login.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateCredentialsRequestDTO {
    private String email;
    private String password;
}