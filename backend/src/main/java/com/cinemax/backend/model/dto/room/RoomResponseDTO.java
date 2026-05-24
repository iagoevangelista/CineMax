package com.cinemax.backend.model.dto.room;

import lombok.Data;

@Data
public class RoomResponseDTO {
    private Integer idRoom;
    private String nameRoom;
    private Integer capacity;
    private String status;
    private Integer idVenue;
    private String venueName;
}