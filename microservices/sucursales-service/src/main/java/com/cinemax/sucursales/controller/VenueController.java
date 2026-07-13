package com.cinemax.sucursales.controller;

import com.cinemax.sucursales.dto.VenueDropdownDTO;
import com.cinemax.sucursales.dto.VenueRequestDTO;
import com.cinemax.sucursales.dto.VenueResponseDTO;
import com.cinemax.sucursales.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/public")
    public ResponseEntity<List<VenueResponseDTO>> getPublicVenues() {
        return ResponseEntity.ok(venueService.getActiveVenues());
    }

    @GetMapping("/dropdown")
    public ResponseEntity<List<VenueDropdownDTO>> getVenuesForDropdown() {
        return ResponseEntity.ok(venueService.getActiveVenuesForDropdown());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<VenueResponseDTO> getVenueById(@PathVariable Integer id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_VENUES')")
    public ResponseEntity<VenueResponseDTO> createVenue(
            @Valid @RequestPart("venue") VenueRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        VenueResponseDTO created = venueService.createVenue(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
}
