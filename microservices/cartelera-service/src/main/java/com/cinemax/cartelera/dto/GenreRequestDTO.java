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
public class GenreRequestDTO {

    @NotBlank(message = "El nombre del género es obligatorio")
    private String nameGenre;
}