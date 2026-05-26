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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('GERENTE_GENERAL', 'GERENTE_DE_MARKETING')")
    public ResponseEntity<SnackResponseDTO> createSnack(
            @RequestParam("snack") String snackJson,
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
    @PreAuthorize("hasAnyAuthority('GERENTE_GENERAL', 'GERENTE_DE_MARKETING')")
    public ResponseEntity<SnackResponseDTO> updateSnack(
            @PathVariable Integer id,
            @RequestParam("snack") String snackJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(file);
        }

        ObjectMapper mapper = new ObjectMapper();
        SnackRequestDTO request = mapper.readValue(snackJson, SnackRequestDTO.class);

        return ResponseEntity.ok(snackService.updateSnack(id, request, imageUrl));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GERENTE_GENERAL', 'GERENTE_DE_MARKETING')")
    public ResponseEntity<Void> deleteSnack(@PathVariable Integer id) {
        snackService.deleteSnack(id);
        return ResponseEntity.noContent().build();
    }
}