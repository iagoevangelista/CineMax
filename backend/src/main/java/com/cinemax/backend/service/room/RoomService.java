package com.cinemax.backend.service.room;

import com.cinemax.backend.model.dto.room.RoomRequestDTO;
import com.cinemax.backend.model.dto.room.RoomResponseDTO;
import java.util.List;

public interface RoomService {
    List<RoomResponseDTO> getAllRooms();
    List<RoomResponseDTO> getRoomsByVenue(Integer idVenue);
    RoomResponseDTO createRoom(RoomRequestDTO request);
    RoomResponseDTO updateRoom(Integer idRoom, RoomRequestDTO request);
}