package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.snack.SnackRequestDTO;
import com.cinemax.backend.model.dto.snack.SnackResponseDTO;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.cinemax.backend.service.snack.SnackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackService snackService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<List<SnackResponseDTO>> getAllSnacks() {
        return ResponseEntity.ok(snackService.getAllSnacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnackResponseDTO> getSnackById(@PathVariable Integer id) {
        return ResponseEntity.ok(snackService.getSnackById(id));
    }

    @GetMapping("/category/{idCategory}")
    public ResponseEntity<List<SnackResponseDTO>> getSnacksByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(snackService.getSnacksByCategory(idCategory));
    }

    @GetMapping("/venue/{idVenue}")
    public ResponseEntity<List<SnackResponseDTO>> getSnacksByVenue(@PathVariable Integer idVenue) {
        return ResponseEntity.ok(snackService.getSnacksByVenue(idVenue));
    }

    @GetMapping("/venue/{idVenue}/admin")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<List<SnackResponseDTO>> getSnacksByVenueAdmin(@PathVariable Integer idVenue) {
        return ResponseEntity.ok(snackService.getSnacksByVenueAdmin(idVenue));
    }

    @GetMapping("/{idSnack}/sedes-count")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Integer> contarSedes(@PathVariable Integer idSnack) {
        return ResponseEntity.ok(snackService.contarSedesConSnack(idSnack));
    }

    @PatchMapping("/{idSnack}/venue/{idVenue}/inhabilitar")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> inhabilitarSnackEnSede(
            @PathVariable Integer idSnack,
            @PathVariable Integer idVenue) {
        snackService.inhabilitarSnackEnSede(idSnack, idVenue);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idSnack}/venue/{idVenue}/habilitar")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> habilitarSnackEnSede(
            @PathVariable Integer idSnack,
            @PathVariable Integer idVenue) {
        snackService.habilitarSnackEnSede(idSnack, idVenue);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idSnack}/venue/{idVenue}/stock")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> actualizarStockEnSede(
            @PathVariable Integer idSnack,
            @PathVariable Integer idVenue,
            @RequestParam Integer stock) {
        snackService.actualizarStockEnSede(idSnack, idVenue, stock);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idSnack}/venue/{idVenue}/agregar")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> agregarSnackASede(
            @PathVariable Integer idSnack,
            @PathVariable Integer idVenue,
            @RequestParam Integer stock) {
        snackService.agregarSnackASede(idSnack, idVenue, stock);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idSnack}/venue/todas/agregar")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> agregarSnackATodasLasSedes(
            @PathVariable Integer idSnack,
            @RequestParam Integer stock) {
        snackService.agregarSnackATodasLasSedes(idSnack, stock);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<SnackResponseDTO> createSnack(
            @RequestPart("snack") String snackJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(file);
        }
        ObjectMapper mapper = new ObjectMapper();
        SnackRequestDTO request = mapper.readValue(snackJson, SnackRequestDTO.class);
        return ResponseEntity.ok(snackService.createSnack(request, imageUrl));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<SnackResponseDTO> updateSnack(
            @PathVariable Integer id,
            @RequestPart("snack") String snackJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(file);
        }
        ObjectMapper mapper = new ObjectMapper();
        SnackRequestDTO request = mapper.readValue(snackJson, SnackRequestDTO.class);
        return ResponseEntity.ok(snackService.updateSnack(id, request, imageUrl));
    }

    // Eliminar snack solo de una sede (borra de snack_venue_stock)
@DeleteMapping("/{idSnack}/venue/{idVenue}")
@PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
public ResponseEntity<Void> eliminarSnackDeSede(
        @PathVariable Integer idSnack,
        @PathVariable Integer idVenue) {
    snackService.eliminarSnackDeSede(idSnack, idVenue);
    return ResponseEntity.noContent().build();
}

// Eliminar snack de todas las sedes y de la tabla snack
@DeleteMapping("/{idSnack}/venue/todas")
@PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
public ResponseEntity<Void> eliminarSnackDeTodo(
        @PathVariable Integer idSnack) {
    snackService.eliminarSnackDeTodo(idSnack);
    return ResponseEntity.noContent().build();
}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CONFITERIA')")
    public ResponseEntity<Void> deleteSnack(@PathVariable Integer id) {
        snackService.deleteSnack(id);
        return ResponseEntity.noContent().build();
    }
}