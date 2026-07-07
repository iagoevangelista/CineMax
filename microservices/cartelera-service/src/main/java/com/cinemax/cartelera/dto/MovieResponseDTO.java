package com.cinemax.cartelera.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDTO {

    private String idMovie;

    private String titleMovie;

    private String synopsis;

    private Integer durationMinutes;

    private String posterUrl;

    private LocalDate releaseDate;

    private String status;

    private LocalDateTime createdAt;

    private Boolean isActive;

    private Boolean premiereWeek;

    private String director;

    // Aquí devolvemos los géneros completos (no solo ids), para que el frontend no tenga que hacer otra petición
    private List<GenreResponseDTO> genres;

    private ClassificationResponseDTO classification;
}