package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.service.showtime.ShowtimeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/by-venue")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> getShowtimesByVenue(
            @RequestParam Integer idVenue,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean isGerGeneral = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(r -> r.equals("ROLE_GERENTE_GENERAL"));

            if (!isGerGeneral && callerVenueId != null && !callerVenueId.equals(idVenue)) {
                return ResponseEntity.status(403).body("No tienes permiso para ver funciones de otra sede.");
            }

            return ResponseEntity.ok(showtimeService.getShowtimesByVenueAndDate(idVenue, date));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ShowtimeSummaryDTO> getSummary(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeSummary(id));
    }

    @GetMapping("/{id}/fares")
    public ResponseEntity<List<TicketFareDTO>> getTicketFares(@PathVariable Integer id) {
        return ResponseEntity.ok(showtimeService.getTicketFares(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> createShowtime(
            @Valid @RequestBody ShowtimeRequestDTO request,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean isGerGeneral = isGerGeneral(auth);
            ShowtimeDTO created = showtimeService.createShowtime(request, isGerGeneral ? null : callerVenueId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> updateShowtime(
            @PathVariable Integer id,
            @Valid @RequestBody ShowtimeRequestDTO request,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean isGerGeneral = isGerGeneral(auth);
            ShowtimeDTO updated = showtimeService.updateShowtime(id, request, isGerGeneral ? null : callerVenueId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<?> cancelShowtime(@PathVariable Integer id, Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean isGerGeneral = isGerGeneral(auth);
            showtimeService.cancelShowtime(id, isGerGeneral ? null : callerVenueId);
            return ResponseEntity.ok("Función cancelada exitosamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Integer extractVenueId(Authentication auth) {
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof com.cinemax.backend.model.entity.UserAccount user) {
            return user.getVenue() != null ? user.getVenue().getIdVenue() : null;
        }
        return null;
    }

    private boolean isGerGeneral(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_GERENTE_GENERAL"));
    }
}