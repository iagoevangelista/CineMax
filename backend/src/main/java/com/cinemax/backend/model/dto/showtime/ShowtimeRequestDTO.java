package com.cinemax.backend.model.dto.showtime;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeRequestDTO {
    private Integer idMovie;
    private Integer idRoom;
    private LocalDate showDate;
    private LocalTime startTime;
    private String languageFormat;
    private BigDecimal baseTicketPrice;
}