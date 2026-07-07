package com.cinemax.cartelera.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cinemax.cartelera.dto.ShowtimeRequestDTO;
import com.cinemax.cartelera.dto.ShowtimeResponseDTO;
import com.cinemax.cartelera.entity.Movie;
import com.cinemax.cartelera.entity.Showtime;
import com.cinemax.cartelera.repository.MovieRepository;
import com.cinemax.cartelera.repository.ShowtimeRepository;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    public List<ShowtimeResponseDTO> findAll() {
        return showtimeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ShowtimeResponseDTO> findByMovieId(String movieId) {
        return showtimeRepository.findByMovieId(movieId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<ShowtimeResponseDTO> findByShowDate(LocalDate showDate) {
        return showtimeRepository.findByShowDate(showDate)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ShowtimeResponseDTO findById(String id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Función no encontrada con id: " + id));
        return toResponseDTO(showtime);
    }

    public ShowtimeResponseDTO create(ShowtimeRequestDTO dto) {
        // Validamos que la película exista
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Película no encontrada con id: " + dto.getMovieId()));

        // Validamos que la hora de fin sea después de la hora de inicio
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("La hora de fin no puede ser antes que la hora de inicio");
        }

        Showtime showtime = Showtime.builder()
                .showDate(dto.getShowDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .languageFormat(dto.getLanguageFormat())
                .baseTicketPrice(dto.getBaseTicketPrice())
                .availableSeats(dto.getAvailableSeats())
                .status(dto.getStatus() != null ? dto.getStatus() : "Programada")
                .movieId(dto.getMovieId())
                .idRoom(dto.getIdRoom())
                .build();

        return toResponseDTO(showtimeRepository.save(showtime), movie);
    }

    public ShowtimeResponseDTO update(String id, ShowtimeRequestDTO dto) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Función no encontrada con id: " + id));

        // Si viene un nuevo movieId, validamos que exista
        if (dto.getMovieId() != null) {
            movieRepository.findById(dto.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Película no encontrada con id: " + dto.getMovieId()));
            showtime.setMovieId(dto.getMovieId());
        }

        if (dto.getEndTime() != null && dto.getStartTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new RuntimeException("La hora de fin no puede ser antes que la hora de inicio");
        }

        showtime.setShowDate(dto.getShowDate());
        showtime.setStartTime(dto.getStartTime());
        showtime.setEndTime(dto.getEndTime());
        showtime.setLanguageFormat(dto.getLanguageFormat());
        showtime.setBaseTicketPrice(dto.getBaseTicketPrice());
        showtime.setAvailableSeats(dto.getAvailableSeats());
        showtime.setIdRoom(dto.getIdRoom());

        if (dto.getStatus() != null) {
            showtime.setStatus(dto.getStatus());
        }

        return toResponseDTO(showtimeRepository.save(showtime));
    }

    public void delete(String id) {
        if (!showtimeRepository.existsById(id)) {
            throw new RuntimeException("Función no encontrada con id: " + id);
        }
        showtimeRepository.deleteById(id);
    }

    private ShowtimeResponseDTO toResponseDTO(Showtime showtime) {
        Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
        return toResponseDTO(showtime, movie);
    }

    private ShowtimeResponseDTO toResponseDTO(Showtime showtime, Movie movie) {
        return ShowtimeResponseDTO.builder()
                .idShowtime(showtime.getIdShowtime())
                .showDate(showtime.getShowDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .languageFormat(showtime.getLanguageFormat())
                .baseTicketPrice(showtime.getBaseTicketPrice())
                .availableSeats(showtime.getAvailableSeats())
                .status(showtime.getStatus())
                .movieId(showtime.getMovieId())
                .movieTitle(movie != null ? movie.getTitleMovie() : null)
                .idRoom(showtime.getIdRoom())
                .build();
    }
}