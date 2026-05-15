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
    private String roleName;   
    private String venueName;   
    private String status;      

}