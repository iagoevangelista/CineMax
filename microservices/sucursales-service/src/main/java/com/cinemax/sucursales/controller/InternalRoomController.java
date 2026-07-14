package com.cinemax.sucursales.controller;

import com.cinemax.sucursales.dto.RoomVenueDTO;
import com.cinemax.sucursales.entity.Room;
import com.cinemax.sucursales.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/rooms")
@RequiredArgsConstructor
public class InternalRoomController {

    private final RoomRepository roomRepository;

    // Usado por facturacion-service para mostrar sala + sucursal en el historial de compras
    // (showtime.idRoom viene de cartelera-service, pero el nombre real de la sala y la
    // sucursal viven acá).
    @GetMapping("/{idRoom}")
    public ResponseEntity<RoomVenueDTO> getRoomWithVenue(@PathVariable Integer idRoom) {
        Room room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RuntimeException("Sala ID " + idRoom + " no encontrada."));

        RoomVenueDTO dto = new RoomVenueDTO();
        dto.setIdRoom(room.getIdRoom());
        dto.setNameRoom(room.getNameRoom());
        if (room.getVenue() != null) {
            dto.setIdVenue(room.getVenue().getIdVenue());
            dto.setNameVenue(room.getVenue().getNameVenue());
        }
        return ResponseEntity.ok(dto);
    }
}