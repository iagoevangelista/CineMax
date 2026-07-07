package com.cinemax.cartelera.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRequestDTO {

    @NotBlank(message = "El nombre de la clasificación es obligatorio")
    private String nameClassification;

    private String descriptionText;
}