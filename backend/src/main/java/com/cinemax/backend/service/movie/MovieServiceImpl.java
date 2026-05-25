package com.cinemax.backend.service.movie;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.dto.movie.MovieRequestDTO;
import com.cinemax.backend.model.entity.Classification;
import com.cinemax.backend.model.entity.Genre;
import com.cinemax.backend.model.entity.Movie;
import com.cinemax.backend.repository.ClassificationRepository;
import com.cinemax.backend.repository.GenreRepository;
import com.cinemax.backend.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional 
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ClassificationRepository classificationRepository;
    private final GenreRepository genreRepository;

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
    public MovieDetailDTO createMovie(MovieRequestDTO request, String imageUrl) {
        
        // Traducir el ID de clasificación a un objeto real de la BD
        Classification classification = classificationRepository.findById(request.getIdClassification())
            .orElseThrow(() -> new RuntimeException("La clasificación seleccionada no existe."));

        // Traducir la lista de IDs de géneros a una lista de objetos Genre reales
        List<Genre> genres = genreRepository.findAllById(request.getIdGenres());
        if (genres.isEmpty()) {
            throw new RuntimeException("Debe asociar al menos un género válido a la película.");
        }

        // Construir la entidad usando el patrón Builder que tiene tu clase Movie
        Movie nuevaPelicula = Movie.builder()
            .titleMovie(request.getTitleMovie())
            .director(request.getDirector())
            .synopsis(request.getSynopsis())
            .durationMinutes(request.getDurationMinutes())
            .releaseDate(request.getReleaseDate())
            .status(request.getStatus())
            .premiereWeek(request.getPremiereWeek() != null ? request.getPremiereWeek() : false)
            .posterUrl(imageUrl) // La URL segura devuelta por Cloudinary
            .classification(classification) // Asignamos el objeto relacional
            .genres(genres)                 // Asignamos la lista relacional
            .isActive(true)                 // Nace activa por defecto
            .build();

        // Guardar físicamente en SQL Server Enterprise Edition
        Movie peliculaGuardada = movieRepository.save(nuevaPelicula);

        // Convertir el resultado a DTO para responderle a Angular de forma limpia
        return convertToDetailDTO(peliculaGuardada);
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


    private MovieDetailDTO convertToDetailDTO(Movie peli) {
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

        if (peli.getGenres() != null) {
            List<String> nombresGeneros = peli.getGenres().stream()
                .map(Genre::getNameGenre)
                .toList();
            dto.setGenreNames(nombresGeneros);
        }

        return dto;
    }
    


    @Override
    public MovieDetailDTO updateMovie(Integer id, MovieRequestDTO request, String imageUrl) {
        Movie peliculaExistente = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));

        Classification classification = classificationRepository.findById(request.getIdClassification())
            .orElseThrow(() -> new RuntimeException("La clasificación seleccionada no existe."));

        List<Genre> genres = genreRepository.findAllById(request.getIdGenres());
        if (genres.isEmpty()) {
            throw new RuntimeException("Debe asociar al menos un género válido.");
        }

        peliculaExistente.setTitleMovie(request.getTitleMovie());
        peliculaExistente.setDirector(request.getDirector());
        peliculaExistente.setSynopsis(request.getSynopsis());
        peliculaExistente.setDurationMinutes(request.getDurationMinutes());
        peliculaExistente.setReleaseDate(request.getReleaseDate());
        peliculaExistente.setStatus(request.getStatus());
        peliculaExistente.setPremiereWeek(request.getPremiereWeek() != null ? request.getPremiereWeek() : false);
        peliculaExistente.setClassification(classification);
        peliculaExistente.setGenres(genres);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            peliculaExistente.setPosterUrl(imageUrl);
        }

        Movie peliculaActualizada = movieRepository.save(peliculaExistente);

        return convertToDetailDTO(peliculaActualizada);
    }


    @Override
    public void deleteMovie(Integer id) {
        Movie peli = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));
        
        peli.setIsActive(false);
        
        movieRepository.save(peli);
    }
    
}
