package com.cinemax.confiteria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackCategoryRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nameCategory;
}