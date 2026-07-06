package com.cinemax.sucursales.service;

import com.cinemax.sucursales.dto.RoomRequestDTO;
import com.cinemax.sucursales.dto.RoomResponseDTO;

import java.util.List;

public interface RoomService {
    List<RoomResponseDTO> getAllRooms();
    List<RoomResponseDTO> getRoomsByVenue(Integer idVenue);
    RoomResponseDTO createRoom(RoomRequestDTO request);
    RoomResponseDTO updateRoom(Integer idRoom, RoomRequestDTO request);
}