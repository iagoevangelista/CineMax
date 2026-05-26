package com.cinemax.backend.model.dto.showtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeDTO {
    private Integer idShowtime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate showDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String languageFormat;
    private String status;
    private BigDecimal baseTicketPrice;
    private Integer availableSeats;

    private Integer idMovie;
    private String titleMovie;
    private Integer durationMinutes;

    private Integer idRoom;
    private String nameRoom;
    private Integer roomCapacity;

    private Integer idVenue;
    private String nameVenue;
}