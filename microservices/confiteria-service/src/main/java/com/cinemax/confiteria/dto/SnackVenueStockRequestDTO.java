package com.cinemax.confiteria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackVenueStockRequestDTO {

    @NotNull(message = "El snack es obligatorio")
    private Integer snackId;

    @NotNull(message = "La sucursal es obligatoria")
    private Integer idVenue;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private String status;
}