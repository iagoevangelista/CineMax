package com.cinemax.backend.model.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TicketFareDTO {
    private String categoryCode;   // "ADULTO", "NINO", "ADULTO_MAYOR", "DISCAPACITADO"
    private String categoryName;   // "Adulto", "Niño", etc.
    private BigDecimal price;
}