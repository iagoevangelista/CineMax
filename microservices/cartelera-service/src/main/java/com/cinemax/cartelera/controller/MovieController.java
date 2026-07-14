package com.cinemax.cartelera.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.*;

import com.cinemax.cartelera.dto.MovieRequestDTO;
import com.cinemax.cartelera.dto.MovieResponseDTO;
import com.cinemax.cartelera.service.MovieService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

   @GetMapping
public ResponseEntity<List<MovieResponseDTO>> findAll(
        @RequestParam(required = false) String status) {
    if (status != null) {
        return ResponseEntity.ok(movieService.findByStatus(status));
    }
    return ResponseEntity.ok(movieService.findAll());
}

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MovieResponseDTO>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(movieService.findByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_MOVIES')") 
    public ResponseEntity<MovieResponseDTO> create(@Valid @RequestBody MovieRequestDTO dto) {
        MovieResponseDTO created = movieService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_MOVIES')") 
    public ResponseEntity<MovieResponseDTO> update(@PathVariable String id, @Valid @RequestBody MovieRequestDTO dto) {
        return ResponseEntity.ok(movieService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_MOVIES')")  
    public ResponseEntity<Void> delete(@PathVariable String id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}