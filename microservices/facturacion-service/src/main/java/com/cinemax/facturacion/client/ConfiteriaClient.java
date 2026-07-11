package com.cinemax.facturacion.client;

import com.cinemax.facturacion.dto.external.SnackStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "confiteria-service")
public interface ConfiteriaClient {

    @GetMapping("/api/snack-venue-stock")
    List<SnackStockDTO> findBySnack(@RequestParam("snackId") Integer snackId);

    @PutMapping("/api/snack-venue-stock/{idSnackVenueStock}")
    SnackStockDTO updateStock(@PathVariable("idSnackVenueStock") Integer idSnackVenueStock,
                                @RequestBody SnackStockDTO body);
}