package com.cinemax.backend.model.dto.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer idUser;
    private String firstName;
    private String lastName;
    private String email;
    private String roleName; 
    private Integer idRole; 
}