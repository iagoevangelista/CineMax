package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.venue.VenueRequestDTO;
import com.cinemax.backend.model.dto.venue.VenueDropdownDTO;
import com.cinemax.backend.model.dto.venue.VenueResponseDTO;
import com.cinemax.backend.service.venue.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    // Solo admin puede ver todas las sedes (para asignar gerentes), el gerente general también puede verlas pero no las necesita para nada específico
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_VENUES')")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<VenueResponseDTO> createVenue(
            @Valid @RequestPart("venue") VenueRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(venueService.createVenue(request, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @PathVariable Integer id,
            @Valid @RequestPart("venue") VenueRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(venueService.updateVenue(id, request, image));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Integer id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    // El admin lo usa para saber qué sedes están disponibles al asignar gerentes
    @GetMapping("/available-for-role/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_VENUES')")
    public ResponseEntity<List<VenueDropdownDTO>> getAvailableVenuesForRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(venueService.getVenuesWithoutRole(roleId));
    }

    // Público: para la página de sedes del cliente
    @GetMapping("/public")
    public ResponseEntity<List<VenueResponseDTO>> getPublicVenues() {
        List<VenueResponseDTO> sedesActivas = venueService.getAllVenues().stream()
                .filter(v -> "Activo".equalsIgnoreCase(v.getStatus()))
                .toList();
        return ResponseEntity.ok(sedesActivas);
    }
}
