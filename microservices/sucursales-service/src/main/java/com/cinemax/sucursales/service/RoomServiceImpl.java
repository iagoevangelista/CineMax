package com.cinemax.sucursales.service;

import com.cinemax.sucursales.dto.RoomRequestDTO;
import com.cinemax.sucursales.dto.RoomResponseDTO;
import com.cinemax.sucursales.entity.Room;
import com.cinemax.sucursales.entity.Seat;
import com.cinemax.sucursales.entity.SeatType;
import com.cinemax.sucursales.entity.Venue;
import com.cinemax.sucursales.repository.RoomRepository;
import com.cinemax.sucursales.repository.SeatRepository;
import com.cinemax.sucursales.repository.SeatTypeRepository;
import com.cinemax.sucursales.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getRoomsByVenue(Integer idVenue) {
        return roomRepository.findByVenue_IdVenue(idVenue).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO request) {
        if (roomRepository.existsByNameRoomAndVenue_IdVenue(request.getNameRoom(), request.getIdVenue())) {
            throw new RuntimeException("Ya existe una sala con el nombre '" + request.getNameRoom() + "' en este cine.");
        }

        int totalAsientosMatriz = request.getNumRows() * request.getSeatsPerRow();
        if (totalAsientosMatriz > request.getCapacity()) {
            throw new RuntimeException("La distribución (" + totalAsientosMatriz +
                    " butacas) supera la capacidad total permitida (" + request.getCapacity() + ").");
        }

        Venue venue = venueRepository.findById(request.getIdVenue())
                .orElseThrow(() -> new RuntimeException("La sede seleccionada no existe"));

        Room room = Room.builder()
                .nameRoom(request.getNameRoom())
                .capacity(request.getCapacity())
                .numRows(request.getNumRows())
                .seatsPerRow(request.getSeatsPerRow())
                .status("Activo")
                .venue(venue)
                .build();

        Room savedRoom = roomRepository.save(room);

        SeatType tipoRegular = seatTypeRepository.findByNameSeatType("REGULAR")
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo de asiento 'REGULAR' no está configurado en seat_type"));

        List<Seat> seatsToSave = new ArrayList<>();
        for (int r = 0; r < request.getNumRows(); r++) {
            String rowLetter = String.valueOf((char) ('A' + r));
            for (int c = 1; c <= request.getSeatsPerRow(); c++) {
                Seat seat = Seat.builder()
                        .room(savedRoom)
                        .rowName(rowLetter)
                        .columnNumber(c)
                        .status("ACTIVO")
                        .seatType(tipoRegular)
                        .build();
                seatsToSave.add(seat);
            }
        }

        seatRepository.saveAll(seatsToSave);

        return mapToDTO(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(Integer idRoom, RoomRequestDTO request) {
        Room room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RuntimeException("La sala no existe"));

        Venue venue = venueRepository.findById(request.getIdVenue())
                .orElseThrow(() -> new RuntimeException("La sede seleccionada no existe"));

        room.setNameRoom(request.getNameRoom());
        room.setCapacity(request.getCapacity());
        room.setStatus(request.getStatus());
        room.setVenue(venue);

        Room updatedRoom = roomRepository.save(room);
        return mapToDTO(updatedRoom);
    }

    private RoomResponseDTO mapToDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setIdRoom(room.getIdRoom());
        dto.setNameRoom(room.getNameRoom());
        dto.setCapacity(room.getCapacity());
        dto.setStatus(room.getStatus());
        dto.setIdVenue(room.getVenue().getIdVenue());
        dto.setVenueName(room.getVenue().getNameVenue());
        return dto;
    }
}