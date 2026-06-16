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

    // Público: el cliente lo necesita para ver funciones de una película
    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@RequestParam Integer idMovie) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(idMovie));
    }

    @GetMapping("/by-venue")
    @PreAuthorize("hasAuthority('MANAGE_SHOWTIMES')")
    public ResponseEntity<?> getShowtimesByVenue(
            @RequestParam Integer idVenue,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);

            // Si no tiene MANAGE_VENUES solo puede ver funciones de su propia sede
            if (!tienePermiso(auth, "MANAGE_VENUES")
                    && callerVenueId != null
                    && !callerVenueId.equals(idVenue)) {
                return ResponseEntity.status(403)
                        .body("No tienes permiso para ver funciones de otra sede.");
            }

            return ResponseEntity.ok(showtimeService.getShowtimesByVenueAndDate(idVenue, date));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Públicos: necesarios para el flujo de compra del cliente
    @GetMapping("/{id}/summary")
    public ResponseEntity<ShowtimeSummaryDTO> getSummary(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeSummary(id));
    }

    @GetMapping("/{id}/fares")
    public ResponseEntity<List<TicketFareDTO>> getTicketFares(@PathVariable Integer id) {
        return ResponseEntity.ok(showtimeService.getTicketFares(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_SHOWTIMES')")
    public ResponseEntity<?> createShowtime(
            @Valid @RequestBody ShowtimeRequestDTO request,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            // Gerente General tiene MANAGE_VENUES, puede crear en cualquier sede
            boolean esGerGeneral = tienePermiso(auth, "MANAGE_VENUES");
            ShowtimeDTO created = showtimeService.createShowtime(
                    request, esGerGeneral ? null : callerVenueId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SHOWTIMES')")
    public ResponseEntity<?> updateShowtime(
            @PathVariable Integer id,
            @Valid @RequestBody ShowtimeRequestDTO request,
            Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean esGerGeneral = tienePermiso(auth, "MANAGE_VENUES");
            ShowtimeDTO updated = showtimeService.updateShowtime(
                    id, request, esGerGeneral ? null : callerVenueId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SHOWTIMES')")
    public ResponseEntity<?> cancelShowtime(@PathVariable Integer id, Authentication auth) {
        try {
            Integer callerVenueId = extractVenueId(auth);
            boolean esGerGeneral = tienePermiso(auth, "MANAGE_VENUES");
            showtimeService.cancelShowtime(id, esGerGeneral ? null : callerVenueId);
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

    // Verifica si el usuario autenticado tiene un permiso específico.
    private boolean tienePermiso(Authentication auth, String permiso) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(permiso));
    }
}
