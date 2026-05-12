package com.cinemax.backend.model.dto.showtime;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeSummaryDTO {
    private String titleMovie;
    private String posterUrl;
    private String nameVenue;
    private LocalDate showDate;
    private LocalTime startTime;
    private String languageFormat;
}