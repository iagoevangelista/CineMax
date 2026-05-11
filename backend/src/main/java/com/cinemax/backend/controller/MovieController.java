package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.service.movie.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

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
    
}