package com.cinemax.usuariosservice.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateDTO {
    @NotNull(message = "El ID del rol es obligatorio")
    private Integer idRole;

    private Integer idVenue;
}