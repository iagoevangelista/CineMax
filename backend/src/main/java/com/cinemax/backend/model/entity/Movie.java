package com.cinemax.backend.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
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
@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @Column(name = "id_movie")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovie;
    
    @Column(name = "title_movie", nullable = false, unique = true, length = 150)
    private String titleMovie;

    @Column(name = "synopsis", columnDefinition = "VARCHAR(MAX)")
    private String synopsis;

    @Positive(message = "Duration must be a positive integer")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "poster_url", length = 255)
    private String posterUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Builder.Default
    @Column(name = "status", length = 20)
<<<<<<< HEAD
    @Pattern(regexp = "Cartelera|Proximamente|Retirada", message = "Status must be either 'Cartelera', 'Proximamente' or 'Retirada'")
    private String status = "Cartelera";

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "premiere_week")
    private Boolean premiereWeek = false;

    @ManyToMany
    @JoinTable(
        name = "movie_genre_map", 
        joinColumns = @JoinColumn(name = "id_movie"), 
        inverseJoinColumns = @JoinColumn(name = "id_genre") 
    )
    private List<Genre> genres;
=======
    @Pattern(regexp = "Cartelera|Estreno|Preventa", message = "Status must be either 'Cartelera', 'Estreno' or 'Preventa'")
    private String status = "Cartelera";
>>>>>>> appmod/java-upgrade-20260509084445

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "premiere_week")
    private Boolean premiereWeek = false;

    @Column(name = "director", length = 150, nullable = false)
    private String director;

    @ManyToMany
    @JoinTable(
        name = "movie_genre_map", 
        joinColumns = @JoinColumn(name = "id_movie"), 
        inverseJoinColumns = @JoinColumn(name = "id_genre") 
    )
    private List<Genre> genres;

    @ManyToOne
    @JoinColumn(name = "id_classification", nullable = false)
    private Classification classification;
}
