package com.cinemax.backend.model.dto.seat;

import lombok.Data;

@Data
public class SeatStatusDTO {
    private Integer idSeat;
    private String rowLetter;
    private Integer columnNumber;
    private String status;        // "ACTIVO", "MANTENIMIENTO", "OCULTO"
    private String nameSeatType;  // "REGULAR" o "WHEELCHAIR"
    private Boolean isOccupied;   // true si ya tiene ticket vendido para esta función
}