package com.cinemax.cartelera.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {

    @Id
    private String idMovie;

    private String titleMovie;

    private String synopsis;

    @Positive(message = "Duration must be a positive integer")
    private Integer durationMinutes;

    private String posterUrl;

    private LocalDate releaseDate;

    @Builder.Default
    @Pattern(regexp = "Cartelera|Estreno|Preventa", message = "Status must be either 'Cartelera', 'Estreno' or 'Preventa'")
    private String status = "Cartelera";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean premiereWeek = false;

    private String director;

    // Antes era @ManyToMany a Genre. Ahora solo guardamos los ids.
    private List<String> genreIds;

    // Antes era @ManyToOne a Classification. Ahora solo el id.
    private String classificationId;
}