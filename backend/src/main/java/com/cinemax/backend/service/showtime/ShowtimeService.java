package com.cinemax.backend.service.showtime;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;

import java.util.List;

public interface ShowtimeService {
    List<ShowtimeDTO> getShowtimesByMovie(Integer idMovie);
    ShowtimeSummaryDTO getShowtimeSummary(Integer idShowtime);
    List<TicketFareDTO> getTicketFares(Integer idShowtime);
    ShowtimeDTO createShowtime(ShowtimeRequestDTO request);
    ShowtimeDTO updateShowtime(Integer id, ShowtimeRequestDTO request);
    void cancelShowtime(Integer id);
}