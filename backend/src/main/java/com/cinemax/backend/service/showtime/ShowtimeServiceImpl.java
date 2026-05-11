package com.cinemax.backend.service.showtime;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.model.entity.Showtime;
import com.cinemax.backend.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

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
}