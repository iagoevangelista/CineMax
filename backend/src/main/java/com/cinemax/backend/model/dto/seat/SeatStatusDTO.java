package com.cinemax.backend.model.dto.seat;

import lombok.Data;

@Data
public class SeatStatusDTO {
    private Integer idSeat;
    private String rowLetter;
    private Integer columnNumber;
    private String nameSeatType; 
    private Boolean isOccupied;
}