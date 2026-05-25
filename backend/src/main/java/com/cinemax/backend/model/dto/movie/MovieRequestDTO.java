package com.cinemax.backend.model.dto.movie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    private String titleMovie;

    @NotBlank(message = "El director es obligatorio")
    private String director;

    private String synopsis;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser un número positivo")
    private Integer durationMinutes;

    private LocalDate releaseDate;

    @NotBlank(message = "El estado es obligatorio")
    private String status; // Ej: "Cartelera", "Estreno"

    private Boolean premiereWeek;
    
    @NotNull(message = "Debe seleccionar una clasificación")
    private Integer idClassification; // Angular nos enviará el ID (ej: 1)

    @NotNull(message = "Debe seleccionar al menos un género")
    private List<Integer> idGenres;   // Angular nos enviará un arreglo de IDs (ej: [2, 4, 5])
}