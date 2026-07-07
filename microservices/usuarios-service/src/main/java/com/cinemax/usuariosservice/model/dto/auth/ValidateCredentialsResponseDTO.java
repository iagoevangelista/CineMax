package com.cinemax.usuariosservice.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCredentialsResponseDTO {
    private boolean valid;
    private String email;
    private String role;
    private String firstName;
    private Integer idVenue;
    private List<String> permissions;
}