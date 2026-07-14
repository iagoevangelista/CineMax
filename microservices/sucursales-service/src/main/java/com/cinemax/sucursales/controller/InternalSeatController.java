package com.cinemax.sucursales.controller;

import com.cinemax.sucursales.dto.SeatDTO;
import com.cinemax.sucursales.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/seats")
@RequiredArgsConstructor
public class InternalSeatController {

    private final SeatRepository seatRepository;

    @GetMapping("/{idSeat}")
    public ResponseEntity<SeatDTO> getSeat(@PathVariable Integer idSeat) {
        return seatRepository.findById(idSeat)
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    dto.setIdSeat(seat.getIdSeat());
                    dto.setRowName(seat.getRowName());
                    dto.setColumnNumber(seat.getColumnNumber());
                    dto.setStatus(seat.getStatus());
                    return ResponseEntity.ok(dto);
                })
                .orElseThrow(() -> new RuntimeException("Asiento ID " + idSeat + " no encontrado."));
    }
}