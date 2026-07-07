package com.cinemax.cartelera.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    private String titleMovie;

    private String synopsis;

    @Positive(message = "Duration must be a positive integer")
    private Integer durationMinutes;

    private String posterUrl;

    private LocalDate releaseDate;

    private String status;

    private Boolean isActive;

    private Boolean premiereWeek;

    @NotBlank(message = "El director es obligatorio")
    private String director;

    private List<String> genreIds;

    @NotBlank(message = "La clasificación es obligatoria")
    private String classificationId;
}