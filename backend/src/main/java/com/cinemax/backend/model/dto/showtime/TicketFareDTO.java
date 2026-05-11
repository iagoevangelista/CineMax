package com.cinemax.backend.model.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TicketFareDTO {
    private String categoryName;
    private BigDecimal price;
}