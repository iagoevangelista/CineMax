package com.cinemax.backend.model.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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
    private String roleName;
    private Integer idVenue;
    private String venueName;
    private String status;
    private String documentNumber;
    private String phone;
    private LocalDate datebirth;
    private String imageUrl;

}