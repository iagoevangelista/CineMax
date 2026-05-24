package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.seat.SeatStatusDTO;
import com.cinemax.backend.service.seat.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8080/api/v1/showtimes")
@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<List<SeatStatusDTO>> getSeatsStatusByShowtime(@RequestParam Integer idShowtime) {
        return ResponseEntity.ok(seatService.getSeatsStatusByShowtime(idShowtime));
    }
}