package com.cinemax.backend.model.dto.user;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserCreateDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer idRole;
    private Integer idVenue;
    private String documentNumber;
    private Integer idDocumentType;
}