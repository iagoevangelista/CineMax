package com.cinemax.backend.service.movie;

import java.util.List;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.dto.movie.MovieRequestDTO;

public interface MovieService {
    List<MovieListDTO> getMoviesByStatus(String status);
    
    MovieDetailDTO getMovieById(Integer id);

    MovieDetailDTO createMovie(MovieRequestDTO request, String imageUrl);

    MovieDetailDTO updateMovie(Integer id, MovieRequestDTO request, String imageUrl);

    void deleteMovie(Integer id);
}