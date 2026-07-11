package com.cinemax.facturacion.client;

import com.cinemax.facturacion.dto.external.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cartelera-service")
public interface CarteleraClient {

    // Ruta real confirmada en ShowtimeController: /api/showtimes/{id} (sin /v1/)
    @GetMapping("/api/showtimes/{id}")
    ShowtimeDTO getShowtime(@PathVariable("id") Integer id);
}