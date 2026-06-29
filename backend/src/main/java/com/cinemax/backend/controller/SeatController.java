package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.seat.SeatDTO;
import com.cinemax.backend.model.dto.seat.SeatStatusDTO;
import com.cinemax.backend.model.entity.Seat;
import com.cinemax.backend.model.entity.SeatType;
import com.cinemax.backend.repository.SeatRepository;
import com.cinemax.backend.service.seat.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final SeatRepository seatRepository;

    // Público: necesario para el flujo de compra del cliente
    @GetMapping
    public ResponseEntity<List<SeatStatusDTO>> getSeatsStatusByShowtime(@RequestParam Integer idShowtime) {
        return ResponseEntity.ok(seatService.getSeatsStatusByShowtime(idShowtime));
    }

    @GetMapping("/room/{idRoom}")
    @PreAuthorize("hasAuthority('MANAGE_SEATS')")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoom(@PathVariable Integer idRoom) {
        List<SeatDTO> seats = seatRepository
                .findByRoom_IdRoomOrderByRowNameAscColumnNumberAsc(idRoom)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seats);
    }

    @PutMapping("/{idSeat}")
    @PreAuthorize("hasAuthority('MANAGE_SEATS')")
    public ResponseEntity<SeatDTO> updateSeat(
            @PathVariable Integer idSeat,
            @RequestBody SeatDTO request) {

        Seat seat = seatRepository.findById(idSeat)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        seat.setStatus(request.getStatus());

        if (request.getIdSeatType() != null) {
            SeatType tipo = new SeatType();
            tipo.setIdSeatType(request.getIdSeatType());
            seat.setSeatType(tipo);
        }

        return ResponseEntity.ok(mapToDTO(seatRepository.save(seat)));
    }

    private SeatDTO mapToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setIdSeat(seat.getIdSeat());
        dto.setRowName(seat.getRowName());
        dto.setColumnNumber(seat.getColumnNumber());
        dto.setStatus(seat.getStatus());
        if (seat.getSeatType() != null) {
            dto.setIdSeatType(seat.getSeatType().getIdSeatType());
        }
        return dto;
    }
}
