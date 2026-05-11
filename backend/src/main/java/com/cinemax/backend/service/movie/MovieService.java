package com.cinemax.backend.service.movie;

import java.util.List;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;

public interface MovieService {
    List<MovieListDTO> getMoviesByStatus(String status);
    
    MovieDetailDTO getMovieById(Integer id);
}