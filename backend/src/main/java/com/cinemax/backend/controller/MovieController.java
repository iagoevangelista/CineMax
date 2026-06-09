package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.dto.movie.MovieRequestDTO;
import com.cinemax.backend.service.movie.MovieService;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final CloudinaryService cloudinaryService;


    @GetMapping
    public ResponseEntity<List<MovieListDTO>> getMoviesByStatus(
            @RequestParam(name = "status") String status) {
        List<MovieListDTO> response = movieService.getMoviesByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailDTO> getMovieById(@PathVariable Integer id) {
        MovieDetailDTO response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    // Administrador
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> createMovie(

            @RequestPart("movie") String movieJson,
            @RequestPart("file") MultipartFile file) {

        try {
            String imageUrl = cloudinaryService.uploadImage(file);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            MovieRequestDTO movieDTO = objectMapper.readValue(movieJson, MovieRequestDTO.class);

            MovieDetailDTO savedMovie = movieService.createMovie(movieDTO, imageUrl);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar los datos o subir el póster: " + e.getMessage());
        } 
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<MovieDetailDTO> updateMovie(
            @PathVariable Integer id,
            @RequestPart("movie") String movieJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            String imageUrl = null;
            if (file != null && !file.isEmpty()) {
                imageUrl = cloudinaryService.uploadImage(file);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            MovieRequestDTO requestDTO = objectMapper.readValue(movieJson, MovieRequestDTO.class);

            MovieDetailDTO updatedMovie = movieService.updateMovie(id, requestDTO, imageUrl);
            
            return ResponseEntity.ok(updatedMovie);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
    


}