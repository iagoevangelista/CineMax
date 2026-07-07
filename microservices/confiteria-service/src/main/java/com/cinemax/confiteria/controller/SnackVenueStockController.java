package com.cinemax.confiteria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinemax.confiteria.dto.SnackVenueStockRequestDTO;
import com.cinemax.confiteria.dto.SnackVenueStockResponseDTO;
import com.cinemax.confiteria.service.SnackVenueStockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/snack-venue-stock")
public class SnackVenueStockController {

    @Autowired
    private SnackVenueStockService snackVenueStockService;

    @GetMapping
    public ResponseEntity<List<SnackVenueStockResponseDTO>> findAll(
            @RequestParam(required = false) Integer venueId,
            @RequestParam(required = false) Integer snackId) {

        if (venueId != null) {
            return ResponseEntity.ok(snackVenueStockService.findByVenue(venueId));
        }
        if (snackId != null) {
            return ResponseEntity.ok(snackVenueStockService.findBySnack(snackId));
        }
        return ResponseEntity.ok(snackVenueStockService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnackVenueStockResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(snackVenueStockService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SnackVenueStockResponseDTO> create(@Valid @RequestBody SnackVenueStockRequestDTO dto) {
        SnackVenueStockResponseDTO created = snackVenueStockService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnackVenueStockResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody SnackVenueStockRequestDTO dto) {
        return ResponseEntity.ok(snackVenueStockService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        snackVenueStockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}