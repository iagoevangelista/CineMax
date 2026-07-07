package com.cinemax.usuariosservice.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer idUser;
    private String firstName;
    private String lastName;
    private String email;
    private Integer idRole;
    private String roleName;
    private Integer idVenue;
    private String status;
    private String documentNumber;
    private String phone;
    private LocalDate datebirth;
    private String imageUrl;
}