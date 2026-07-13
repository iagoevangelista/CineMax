package com.cinemax.confiteria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDescontarRequestDTO {

    @NotNull
    private Integer snackId;

    @NotNull
    private Integer idVenue;

    @NotNull
    @Min(1)
    private Integer quantity;
}