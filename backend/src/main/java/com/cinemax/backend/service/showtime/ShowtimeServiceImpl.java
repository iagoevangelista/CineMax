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

        List<ShowtimeDTO> listaLigeras = new ArrayList<>();
        for (Showtime f : funciones) {
            ShowtimeDTO dto = new ShowtimeDTO();
            dto.setIdShowtime(f.getIdShowtime());
            dto.setShowDate(f.getShowDate());
            dto.setStartTime(f.getStartTime());
            dto.setLanguageFormat(f.getLanguageFormat());

            listaLigeras.add(dto);
        }
        
        return listaLigeras;
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
    public ShowtimeDTO createShowtime(ShowtimeRequestDTO request) {
        
        Movie movie = movieRepository.findById(request.getIdMovie())
                .orElseThrow(() -> new RuntimeException("La película seleccionada no existe."));
                
        Room room = roomRepository.findById(request.getIdRoom())
                .orElseThrow(() -> new RuntimeException("La sala seleccionada no existe."));

        int totalMinutos = movie.getDurationMinutes() + 30;
        LocalTime endTime = request.getStartTime().plusMinutes(totalMinutos);

        boolean hasConflict = showtimeRepository.existsConflictingShowtime(
                room.getIdRoom(), 
                request.getShowDate(), 
                request.getStartTime(), 
                endTime
        );

        if (hasConflict) {
            throw new RuntimeException("ERROR: La Sala " + room.getNameRoom() + 
                    " ya tiene una función programada que cruza con este horario (" + 
                    request.getStartTime() + " - " + endTime + ").");
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

        Showtime savedShowtime = showtimeRepository.save(newShowtime);

        ShowtimeDTO response = new ShowtimeDTO();
        response.setIdShowtime(savedShowtime.getIdShowtime());
        response.setShowDate(savedShowtime.getShowDate());
        response.setStartTime(savedShowtime.getStartTime());
        response.setLanguageFormat(savedShowtime.getLanguageFormat());
        
        return response;
    }

    @Override
    @Transactional
    public ShowtimeDTO updateShowtime(Integer id, ShowtimeRequestDTO request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La función no existe."));

        if (!showtime.getStatus().equals("Programada")) {
                throw new RuntimeException("Solo se pueden editar funciones en estado 'Programada'.");
        }

        Movie movie = movieRepository.findById(request.getIdMovie())
                .orElseThrow(() -> new RuntimeException("La película seleccionada no existe."));
                
        Room room = roomRepository.findById(request.getIdRoom())
                .orElseThrow(() -> new RuntimeException("La sala seleccionada no existe."));

        int totalMinutos = movie.getDurationMinutes() + 30;
        LocalTime endTime = request.getStartTime().plusMinutes(totalMinutos);

        boolean hasConflict = showtimeRepository.existsConflictingShowtime(
                room.getIdRoom(), request.getShowDate(), request.getStartTime(), endTime
        );
        
        if (hasConflict) {
            List<Showtime> functionsInRoom = showtimeRepository.findAll().stream()
                .filter(s -> s.getRoom().getIdRoom().equals(room.getIdRoom()) && 
                                s.getShowDate().equals(request.getShowDate()) &&
                                !s.getIdShowtime().equals(id) && 
                                !s.getStatus().equals("Cancelada"))
                .toList();
            
            for(Showtime s : functionsInRoom) {
                if(request.getStartTime().isBefore(s.getEndTime()) && endTime.isAfter(s.getStartTime())) {
                    throw new RuntimeException("Conflicto de Horarios al actualizar: La sala ya está ocupada.");
                }
            }
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setShowDate(request.getShowDate());
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(endTime);
        showtime.setLanguageFormat(request.getLanguageFormat());
        showtime.setBaseTicketPrice(request.getBaseTicketPrice());

        showtime.setAvailableSeats(room.getCapacity()); 

        return mapToDTO(showtimeRepository.save(showtime));
    }

    @Override
    @Transactional
    public void cancelShowtime(Integer id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La función no existe."));
        
        showtime.setStatus("Cancelada");
        showtimeRepository.save(showtime);
    }

    private ShowtimeDTO mapToDTO(Showtime f) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setIdShowtime(f.getIdShowtime());
        dto.setShowDate(f.getShowDate());
        dto.setStartTime(f.getStartTime());
        dto.setLanguageFormat(f.getLanguageFormat());
        return dto;
    }


}