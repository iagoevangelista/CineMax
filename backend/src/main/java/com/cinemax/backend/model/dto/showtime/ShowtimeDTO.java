package com.cinemax.backend.model.dto.showtime;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeDTO {
    private Integer idShowtime;
    private LocalDate showDate;
    private LocalTime startTime;
    private String languageFormat; 
}