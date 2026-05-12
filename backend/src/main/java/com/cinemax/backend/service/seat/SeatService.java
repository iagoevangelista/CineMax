package com.cinemax.backend.service.seat;

import com.cinemax.backend.model.dto.seat.SeatStatusDTO;
import java.util.List;

public interface SeatService {
    List<SeatStatusDTO> getSeatsStatusByShowtime(Integer idShowtime);
}