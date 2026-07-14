package com.cinemax.facturacion.client;

import com.cinemax.facturacion.dto.external.RoomVenueDTO;
import com.cinemax.facturacion.dto.external.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sucursales-service")
public interface SucursalesClient {

    @GetMapping("/internal/seats/{idSeat}")
    SeatDTO getSeat(@PathVariable("idSeat") Integer idSeat);

    @GetMapping("/internal/rooms/{idRoom}")
    RoomVenueDTO getRoomWithVenue(@PathVariable("idRoom") Integer idRoom);
}