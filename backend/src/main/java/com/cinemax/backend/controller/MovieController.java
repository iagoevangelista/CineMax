package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.service.movie.MovieService;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    // ❌ BORRAMOS la línea de 'private final ObjectMapper objectMapper;' de aquí.

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createMovie(
            @RequestPart("movie") String movieJson,
            @RequestPart("file") MultipartFile file) {

        try {
            // 1. Subir la imagen a Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);

            // ✅ CREAMOS el ObjectMapper directamente aquí adentro
            ObjectMapper objectMapper = new ObjectMapper();

            // 2. Convertir el String JSON a tu DTO
            MovieDetailDTO movieDTO = objectMapper.readValue(movieJson, MovieDetailDTO.class);

            // 3. Asignar la URL generada al DTO
            movieDTO.setPosterUrl(imageUrl);

            // 4. Guardar en BD (Descomenta esto cuando tu servicio esté listo para guardar)
            // MovieDetailDTO savedMovie = movieService.createMovie(movieDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body("Película guardada con URL: " + imageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir la imagen: " + e.getMessage());
        }
    }
}