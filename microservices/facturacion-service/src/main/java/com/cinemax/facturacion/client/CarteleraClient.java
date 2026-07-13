package com.cinemax.facturacion.client;

import com.cinemax.facturacion.dto.external.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cartelera-service")
public interface CarteleraClient {

    // Ruta real confirmada en ShowtimeController: /api/showtimes/{id} (sin /v1, el gateway reescribe internamente)
    @GetMapping("/api/showtimes/{id}")
    ShowtimeDTO getShowtime(@PathVariable("id") Integer id);

    // Igual que el monolito: showtime.setAvailableSeats(availableSeats - seats.size()) al confirmar una venta.
    @PatchMapping("/api/showtimes/{id}/decrease-seats")
    void decreaseAvailableSeats(@PathVariable("id") Integer id, @RequestParam("cantidad") int cantidad);
}