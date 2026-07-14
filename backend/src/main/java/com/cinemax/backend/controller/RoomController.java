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

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ROOMS')")
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/venue/{idVenue}")
    @PreAuthorize("hasAuthority('MANAGE_ROOMS')")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByVenue(@PathVariable Integer idVenue) {
        return ResponseEntity.ok(roomService.getRoomsByVenue(idVenue));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ROOMS')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROOMS')")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Integer id,
            @Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }
}
