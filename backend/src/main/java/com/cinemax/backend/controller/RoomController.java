package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.room.RoomRequestDTO;
import com.cinemax.backend.model.dto.room.RoomResponseDTO;
import com.cinemax.backend.service.room.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // Obtener todas las salas (Lo usará el Admin general)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_OPERACIONES', 'GERENTE_GENERAL', 'GERENTE_OPERACIONES')")
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // Obtener salas de un cine en específico (Súper útil para cuando hagamos las Funciones)
    @GetMapping("/venue/{idVenue}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_OPERACIONES', 'GERENTE_GENERAL', 'GERENTE_OPERACIONES')")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByVenue(@PathVariable Integer idVenue) {
        return ResponseEntity.ok(roomService.getRoomsByVenue(idVenue));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_OPERACIONES', 'GERENTE_GENERAL', 'GERENTE_OPERACIONES')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_OPERACIONES', 'GERENTE_GENERAL', 'GERENTE_OPERACIONES')")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Integer id,
            @Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }
}