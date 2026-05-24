package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.service.showtime.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@RequestParam Integer idMovie) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(idMovie));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ShowtimeSummaryDTO> getSummary(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeSummary(id));
    }

    @GetMapping("/{id}/fares")
    public ResponseEntity<List<TicketFareDTO>> getTicketFares(@PathVariable("idShowtime") Integer id) {
        return ResponseEntity.ok(showtimeService.getTicketFares(id));
    }

}