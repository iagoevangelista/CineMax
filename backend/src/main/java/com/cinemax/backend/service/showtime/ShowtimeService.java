package com.cinemax.backend.service.showtime;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeDTO> getShowtimesByMovie(Integer idMovie);
    List<ShowtimeDTO> getShowtimesByVenueAndDate(Integer idVenue, LocalDate date);
    ShowtimeSummaryDTO getShowtimeSummary(Integer idShowtime);
    List<TicketFareDTO> getTicketFares(Integer idShowtime);
    ShowtimeDTO createShowtime(ShowtimeRequestDTO request, Integer callerVenueId);
    ShowtimeDTO updateShowtime(Integer id, ShowtimeRequestDTO request, Integer callerVenueId);
    void cancelShowtime(Integer id, Integer callerVenueId);
}