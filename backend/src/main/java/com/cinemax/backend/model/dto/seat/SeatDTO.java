package com.cinemax.backend.model.dto.seat;

import lombok.Data;

@Data
public class SeatDTO {
    private Integer idSeat;
    private String rowName;
    private Integer columnNumber;
    private String status;      // "ACTIVO", "MANTENIMIENTO", "OCULTO"
    private String seatType;    // "REGULAR", "WHEELCHAIR"
}