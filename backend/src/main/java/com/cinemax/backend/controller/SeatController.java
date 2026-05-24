package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.seat.SeatDTO;
import com.cinemax.backend.model.dto.seat.SeatStatusDTO;
import com.cinemax.backend.model.entity.Seat;
import com.cinemax.backend.model.entity.SeatType; // <-- ¡Añadida la importación!
import com.cinemax.backend.repository.SeatRepository;
import com.cinemax.backend.service.seat.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:8080/api/v1/showtimes")
@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final SeatRepository seatRepository;

    @GetMapping
    public ResponseEntity<List<SeatStatusDTO>> getSeatsStatusByShowtime(@RequestParam Integer idShowtime) {
        return ResponseEntity.ok(seatService.getSeatsStatusByShowtime(idShowtime));
    }

    @GetMapping("/room/{idRoom}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable Integer idRoom) {
        List<SeatDTO> seats = seatRepository.findByRoom_IdRoomOrderByRowNameAscColumnNumberAsc(idRoom)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seats);
    }

    // 2. ACTUALIZAR ESTADO O TIPO DE UN ASIENTO (Al hacer clic en el panel)
    @PutMapping("/{idSeat}")
    @PreAuthorize("hasAnyAuthority('ROLE_GERENTE_GENERAL', 'ROLE_GERENTE_DE_OPERACIONES')")
    public ResponseEntity<SeatDTO> updateSeat(@PathVariable Integer idSeat, @RequestBody SeatDTO request) {
        Seat seat = seatRepository.findById(idSeat)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        seat.setStatus(request.getStatus());

        // --- SOLUCIÓN PARA LA TABLA SEAT_TYPE ---
        // Construimos el objeto SeatType usando el ID que nos manda Angular
        if (request.getIdSeatType() != null) {
            SeatType tipo = new SeatType();
            tipo.setIdSeatType(request.getIdSeatType());
            seat.setSeatType(tipo);
        }

        Seat updatedSeat = seatRepository.save(seat);
        return ResponseEntity.ok(mapToDTO(updatedSeat));
    }

    // Método auxiliar de mapeo
    private SeatDTO mapToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setIdSeat(seat.getIdSeat());
        dto.setRowName(seat.getRowName());
        dto.setColumnNumber(seat.getColumnNumber());
        dto.setStatus(seat.getStatus());

        // --- SOLUCIÓN PARA LA TABLA SEAT_TYPE ---
        // Extraemos el ID del objeto para enviárselo a Angular
        if (seat.getSeatType() != null) {
            dto.setIdSeatType(seat.getSeatType().getIdSeatType());
        }

        return dto;
    }
}