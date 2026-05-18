package com.cinemax.backend.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "El nombre es obligatorio.")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    private String lastName;

    // AQUÍ VAN LOS 2 CAMPOS EXTRA DE TU ENTIDAD USERACCOUNT (Ej: Teléfono y Dirección)
    // Cambia los nombres si en tu clase UserAccount.java se llaman diferente
    private String phone;
    private LocalDate datebirth;

    // La contraseña es opcional: solo se validará si el usuario escribe algo
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "La nueva contraseña debe tener al menos 8 caracteres, incluyendo una letra y un número.")
    private String newPassword;
    private String oldPassword;
}