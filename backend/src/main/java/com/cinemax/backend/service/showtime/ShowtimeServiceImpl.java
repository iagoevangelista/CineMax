package com.cinemax.backend.service.showtime;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.repository.RoomRepository;
import com.cinemax.backend.repository.MovieRepository;
import com.cinemax.backend.model.entity.Movie;
import com.cinemax.backend.model.entity.Room;
import com.cinemax.backend.model.entity.Showtime;
import com.cinemax.backend.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<ShowtimeDTO> getShowtimesByMovie(Integer idMovie) {
        List<Showtime> funciones = showtimeRepository.findByMovie_IdMovieAndStatus(idMovie, "Programada");
        List<ShowtimeDTO> result = new ArrayList<>();
        for (Showtime f : funciones) {
            result.add(mapToDTO(f));
        }
        return result;
    }

    @Override
    public List<ShowtimeDTO> getShowtimesByVenueAndDate(Integer idVenue, LocalDate date) {
        List<Showtime> funciones = showtimeRepository.findByVenueAndDate(idVenue, date);
        List<ShowtimeDTO> result = new ArrayList<>();
        for (Showtime f : funciones) {
            result.add(mapToDTO(f));
        }
        return result;
    }

    @Override
    public ShowtimeSummaryDTO getShowtimeSummary(Integer idShowtime) {
        Showtime funcion = showtimeRepository.findById(idShowtime)
                .orElseThrow(() -> new RuntimeException("Función no encontrada"));

        ShowtimeSummaryDTO dto = new ShowtimeSummaryDTO();
        dto.setTitleMovie(funcion.getMovie().getTitleMovie());
        dto.setPosterUrl(funcion.getMovie().getPosterUrl());
        dto.setNameVenue(funcion.getRoom().getVenue().getNameVenue());
        dto.setShowDate(funcion.getShowDate());
        dto.setStartTime(funcion.getStartTime());
        dto.setLanguageFormat(funcion.getLanguageFormat());
        return dto;
    }

    @Override
    public List<TicketFareDTO> getTicketFares(Integer idShowtime) {
        Showtime funcion = showtimeRepository.findById(idShowtime)
                .orElseThrow(() -> new RuntimeException("Función no encontrada"));

        BigDecimal precioBase = funcion.getBaseTicketPrice();
        List<TicketFareDTO> tarifas = new ArrayList<>();
        tarifas.add(new TicketFareDTO("Adulto", precioBase));
        tarifas.add(new TicketFareDTO("Niño", precioBase.subtract(new BigDecimal("11.00"))));
        tarifas.add(new TicketFareDTO("Adulto Mayor", precioBase.subtract(new BigDecimal("9.00"))));
        tarifas.add(new TicketFareDTO("Personas Discapacitadas", precioBase.subtract(new BigDecimal("9.00"))));
        return tarifas;
    }

    @Override
    @Transactional
    public ShowtimeDTO createShowtime(ShowtimeRequestDTO request, Integer callerVenueId) {

        Movie movie = movieRepository.findById(request.getIdMovie())
                .orElseThrow(() -> new RuntimeException("La película seleccionada no existe."));

        if (!movie.getIsActive()) {
            throw new RuntimeException("No se puede programar una película inactiva.");
        }

        Room room = roomRepository.findById(request.getIdRoom())
                .orElseThrow(() -> new RuntimeException("La sala seleccionada no existe."));

        if (callerVenueId != null && !room.getVenue().getIdVenue().equals(callerVenueId)) {
            throw new RuntimeException("No tienes permiso para programar funciones en otra sede.");
        }

        if (!"Activo".equals(room.getStatus())) {
            throw new RuntimeException("La sala \"" + room.getNameRoom() + "\" está inactiva y no puede recibir funciones.");
        }

        if (request.getShowDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede programar una función en una fecha pasada.");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes() + 30);

        boolean hasConflict = showtimeRepository.countConflictingShowtime(
                room.getIdRoom(), request.getShowDate(), request.getStartTime(), endTime) > 0;

        if (hasConflict) {
            throw new RuntimeException("La sala \"" + room.getNameRoom() +
                    "\" ya tiene una función que cruza con el horario " +
                    request.getStartTime() + " – " + endTime + ".");
        }

        Showtime newShowtime = Showtime.builder()
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .languageFormat(request.getLanguageFormat())
                .baseTicketPrice(request.getBaseTicketPrice())
                .availableSeats(room.getCapacity())
                .status("Programada")
                .movie(movie)
                .room(room)
                .build();

        return mapToDTO(showtimeRepository.save(newShowtime));
    }

    @Override
    @Transactional
    public ShowtimeDTO updateShowtime(Integer id, ShowtimeRequestDTO request, Integer callerVenueId) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La función no existe."));

        // SEGURIDAD: el gerente solo puede editar funciones de SU sede
        if (callerVenueId != null &&
                !showtime.getRoom().getVenue().getIdVenue().equals(callerVenueId)) {
            throw new RuntimeException("No tienes permiso para editar funciones de otra sede.");
        }

        if (!"Programada".equals(showtime.getStatus())) {
            throw new RuntimeException("Solo se pueden editar funciones en estado 'Programada'.");
        }

        if (showtime.getShowDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede modificar una función de una fecha pasada.");
        }

        Movie movie = movieRepository.findById(request.getIdMovie())
                .orElseThrow(() -> new RuntimeException("La película seleccionada no existe."));

        Room room = roomRepository.findById(request.getIdRoom())
                .orElseThrow(() -> new RuntimeException("La sala seleccionada no existe."));

        // Si cambia de sala, también verificar que sigue siendo de la misma sede
        if (callerVenueId != null && !room.getVenue().getIdVenue().equals(callerVenueId)) {
            throw new RuntimeException("No puedes mover una función a una sala de otra sede.");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes() + 30);

        boolean hasConflict = showtimeRepository.countConflictingShowtimeExcluding(
                room.getIdRoom(), request.getShowDate(), request.getStartTime(), endTime, id) > 0;

        if (hasConflict) {
            throw new RuntimeException("La sala \"" + room.getNameRoom() +
                    "\" ya tiene una función que cruza con el horario " +
                    request.getStartTime() + " – " + endTime + ".");
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setShowDate(request.getShowDate());
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(endTime);
        showtime.setLanguageFormat(request.getLanguageFormat());
        showtime.setBaseTicketPrice(request.getBaseTicketPrice());
        // No reseteamos availableSeats si ya hay ventas; solo si cambia la sala
        if (!showtime.getRoom().getIdRoom().equals(room.getIdRoom())) {
            showtime.setAvailableSeats(room.getCapacity());
        }

        return mapToDTO(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public void cancelShowtime(Integer id, Integer callerVenueId) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La función no existe."));

        if (callerVenueId != null &&
                !showtime.getRoom().getVenue().getIdVenue().equals(callerVenueId)) {
            throw new RuntimeException("No tienes permiso para cancelar funciones de otra sede.");
        }

        if ("Cancelada".equals(showtime.getStatus())) {
            throw new RuntimeException("Esta función ya está cancelada.");
        }
        if ("Finalizada".equals(showtime.getStatus())) {
            throw new RuntimeException("No se puede cancelar una función ya finalizada.");
        }

        showtime.setStatus("Cancelada");
        showtimeRepository.save(showtime);
    }

    private ShowtimeDTO mapToDTO(Showtime f) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setIdShowtime(f.getIdShowtime());
        dto.setShowDate(f.getShowDate());
        dto.setStartTime(f.getStartTime());
        dto.setEndTime(f.getEndTime());
        dto.setLanguageFormat(f.getLanguageFormat());
        dto.setStatus(f.getStatus());
        dto.setBaseTicketPrice(f.getBaseTicketPrice());
        dto.setAvailableSeats(f.getAvailableSeats());

        dto.setIdMovie(f.getMovie().getIdMovie());
        dto.setTitleMovie(f.getMovie().getTitleMovie());
        dto.setDurationMinutes(f.getMovie().getDurationMinutes());

        dto.setIdRoom(f.getRoom().getIdRoom());
        dto.setNameRoom(f.getRoom().getNameRoom());
        dto.setRoomCapacity(f.getRoom().getCapacity());

        dto.setIdVenue(f.getRoom().getVenue().getIdVenue());
        dto.setNameVenue(f.getRoom().getVenue().getNameVenue());

        return dto;
    }
}