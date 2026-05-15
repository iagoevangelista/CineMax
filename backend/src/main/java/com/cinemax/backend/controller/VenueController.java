package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.venue.VenueRequestDTO;
import com.cinemax.backend.model.dto.venue.VenueDropdownDTO;
import com.cinemax.backend.model.dto.venue.VenueResponseDTO;
import com.cinemax.backend.service.venue.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_GERENTE_GRAL')")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_GERENTE_GRAL')")
    public ResponseEntity<VenueResponseDTO> createVenue(@RequestBody VenueRequestDTO request) {
        return ResponseEntity.ok(venueService.createVenue(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_GERENTE_GRAL')")
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @PathVariable Integer id, 
            @RequestBody VenueRequestDTO request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_GERENTE_GRAL')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Integer id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-for-role/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_GERENTE_GRAL')")
    public ResponseEntity<List<VenueDropdownDTO>> getAvailableVenuesForRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(venueService.getVenuesWithoutRole(roleId));
    }

}