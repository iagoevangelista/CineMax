package com.cinemax.backend.model.dto.movie;

import lombok.Data;

@Data
public class MovieListDTO {
    private Integer idMovie;
    private String titleMovie;
    private String posterUrl;
}
