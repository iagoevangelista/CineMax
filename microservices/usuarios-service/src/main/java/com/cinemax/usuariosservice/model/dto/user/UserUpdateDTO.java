package com.cinemax.usuariosservice.model.dto.user;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate datebirth;
    private String oldPassword;
    private String newPassword;
}