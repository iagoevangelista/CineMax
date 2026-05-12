package com.cinemax.backend.model.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDTO {
    private Integer idUser;
    private String firstName;
    private String lastName;
    private String email;
    private Integer idRole;
    private String roleName;    // Ej: "ADMIN", "GERENTE_GRAL"
    private String venueName;   // Ej: "Plaza San Miguel" (Puede ser null)
    private String status;      // Ej: "Activo"

}