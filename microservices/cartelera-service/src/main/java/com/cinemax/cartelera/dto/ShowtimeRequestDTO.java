package com.cinemax.cartelera.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeRequestDTO {

    @NotNull(message = "La fecha de función es obligatoria")
    private LocalDate showDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    @NotBlank(message = "El formato de idioma es obligatorio")
    private String languageFormat;

    @Min(value = 0, message = "El precio base no puede ser negativo")
    private BigDecimal baseTicketPrice;

    private Integer availableSeats;

    private String status;

    @NotBlank(message = "La película es obligatoria")
    private String movieId;

    @NotNull(message = "La sala es obligatoria")
    private Integer idRoom;
}