package com.cinemax.backend.service.movie;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.entity.Movie;
import com.cinemax.backend.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public List<MovieListDTO> getMoviesByStatus(String status) {
        List<Movie> peliculasPesadas = movieRepository.findByStatusAndIsActiveTrue(status);

        List<MovieListDTO> peliculasLigeras = new ArrayList<>();

        for (Movie peli : peliculasPesadas) {
            MovieListDTO dto = new MovieListDTO();
            
            dto.setIdMovie(peli.getIdMovie());
            dto.setTitleMovie(peli.getTitleMovie());
            dto.setPosterUrl(peli.getPosterUrl());
            dto.setStatus(peli.getStatus());
            dto.setPremiereWeek(peli.getPremiereWeek());

            if (peli.getClassification() != null) {
                dto.setRating(peli.getClassification().getNameClassification());
            }
            
            peliculasLigeras.add(dto);
        }

        return peliculasLigeras;
    }

    @Override
    public MovieDetailDTO getMovieById(Integer id) {
        Movie peli = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));

        MovieDetailDTO dto = new MovieDetailDTO();
        dto.setIdMovie(peli.getIdMovie());
        dto.setTitleMovie(peli.getTitleMovie());
        dto.setSynopsis(peli.getSynopsis());
        dto.setDurationMinutes(peli.getDurationMinutes());
        dto.setPosterUrl(peli.getPosterUrl());
        dto.setReleaseDate(peli.getReleaseDate());
        dto.setDirector(peli.getDirector());

        if (peli.getClassification() != null) {
            dto.setClassificationName(peli.getClassification().getNameClassification());
        }

        List<String> nombresGeneros = peli.getGenres().stream()
                .map(genre -> genre.getNameGenre())
                .toList();
        dto.setGenreNames(nombresGeneros);

        return dto;
    }
    
}
