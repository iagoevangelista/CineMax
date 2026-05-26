package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.service.showtime.ShowtimeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@RequestParam Integer idMovie) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(idMovie));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ShowtimeSummaryDTO> getSummary(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeSummary(id));
    }

    @GetMapping("/{id}/fares")
    public ResponseEntity<List<TicketFareDTO>> getTicketFares(@PathVariable("idShowtime") Integer id) {
        return ResponseEntity.ok(showtimeService.getTicketFares(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> createShowtime(@Valid @RequestBody ShowtimeRequestDTO request) {
        try {
            ShowtimeDTO createdShowtime = showtimeService.createShowtime(request);
            return ResponseEntity.ok(createdShowtime);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> updateShowtime(
            @PathVariable Integer id, 
            @Valid @RequestBody ShowtimeRequestDTO request) {
        try {
            ShowtimeDTO updatedShowtime = showtimeService.updateShowtime(id, request);
            return ResponseEntity.ok(updatedShowtime);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL','ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> cancelShowtime(@PathVariable Integer id) {
        try {
            showtimeService.cancelShowtime(id);
            return ResponseEntity.ok("Función cancelada exitosamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}