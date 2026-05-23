package com.cinemax.backend.service.room;

import com.cinemax.backend.model.dto.room.RoomRequestDTO;
import com.cinemax.backend.model.dto.room.RoomResponseDTO;
import com.cinemax.backend.model.entity.Room;
import com.cinemax.backend.model.entity.Seat;
import com.cinemax.backend.model.entity.SeatType;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.RoomRepository;
import org.springframework.transaction.annotation.Transactional;
import com.cinemax.backend.repository.SeatRepository;
import com.cinemax.backend.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    @Override
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponseDTO> getRoomsByVenue(Integer idVenue) {
        // En el futuro puedes agregar un método en el repositorio findByVenue_IdVenue
        // Por ahora filtramos aquí por simplicidad
        return roomRepository.findAll().stream()
                .filter(room -> room.getVenue().getIdVenue().equals(idVenue))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
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

    // Metodo auxiliar para no repetir código de mapeo
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

    @Transactional
    @Override
    public RoomResponseDTO createRoom(RoomRequestDTO request) {

        // 1. VALIDACIÓN (HU-16): Nombre único en la sede
        if (roomRepository.existsByNameRoomAndVenue_IdVenue(request.getNameRoom(), request.getIdVenue())) {
            throw new RuntimeException("Ya existe una sala con el nombre '" + request.getNameRoom() + "' en este cine.");
        }

        // 2. VALIDACIÓN (HU-17): Filas x Columnas no debe superar la Capacidad Total
        int totalAsientosMatriz = request.getNumRows() * request.getSeatsPerRow();
        if (totalAsientosMatriz > request.getCapacity()) {
            throw new RuntimeException("La distribución (" + totalAsientosMatriz + " butacas) supera la capacidad total permitida (" + request.getCapacity() + ").");
        }

        Venue venue = venueRepository.findById(request.getIdVenue())
                .orElseThrow(() -> new RuntimeException("La sede seleccionada no existe"));

        // 3. Guardamos la Sala
        Room room = Room.builder()
                .nameRoom(request.getNameRoom())
                .capacity(request.getCapacity())
                .numRows(request.getNumRows())             // Guardamos Filas
                .seatsPerRow(request.getSeatsPerRow())     // Guardamos Columnas
                .status("Activo")
                .venue(venue)
                .build();

        Room savedRoom = roomRepository.save(room);
        SeatType tipoRegular = new SeatType();
        tipoRegular.setIdSeatType(1);

        // 4. MAGIA (HU-18): Generación Automática del Tablero de Asientos
        List<Seat> seatsToSave = new java.util.ArrayList<>();

        for (int r = 0; r < request.getNumRows(); r++) {
            // Convertimos el número de fila a letra (0='A', 1='B', 2='C'...)
            String rowLetter = String.valueOf((char) ('A' + r));

            for (int c = 1; c <= request.getSeatsPerRow(); c++) {
                Seat seat = Seat.builder()
                        .room(savedRoom)
                        .rowName(rowLetter)
                        .columnNumber(c)
                        .status("ACTIVO")      // Estado Físico: Todos nacen sanos
                        .seatType(tipoRegular)   // Tipo: Butaca normal
                        .build();
                seatsToSave.add(seat);
            }
        }

        // Guardamos los 100 o 200 asientos de golpe en la base de datos
        seatRepository.saveAll(seatsToSave);

        return mapToDTO(savedRoom);
    }
}