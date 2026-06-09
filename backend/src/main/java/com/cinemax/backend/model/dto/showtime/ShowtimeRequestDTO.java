package com.cinemax.backend.model.dto.showtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeRequestDTO {
    private Integer idMovie;
    private Integer idRoom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate showDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    private String languageFormat;
    private BigDecimal baseTicketPrice;
}