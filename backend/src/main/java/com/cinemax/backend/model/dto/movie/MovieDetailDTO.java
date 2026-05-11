package com.cinemax.backend.model.dto.movie;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieDetailDTO {
    private Integer idMovie;
    private String titleMovie;
    private String synopsis;
    private Integer durationMinutes;
    private String posterUrl;
    private LocalDate releaseDate;
    private String classificationName; 
    private List<String> genreNames;   
    private String director;
}