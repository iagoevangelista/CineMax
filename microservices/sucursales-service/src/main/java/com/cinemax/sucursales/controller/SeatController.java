package com.cinemax.sucursales.controller;

import com.cinemax.sucursales.dto.SeatDTO;
import com.cinemax.sucursales.entity.Seat;
import com.cinemax.sucursales.entity.SeatType;
import com.cinemax.sucursales.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;

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