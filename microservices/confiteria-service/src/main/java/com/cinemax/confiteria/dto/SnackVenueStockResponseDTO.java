package com.cinemax.confiteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackVenueStockResponseDTO {

    private Integer idSnackVenueStock;

    private Integer snackId;

    private String snackName;

    private Integer idVenue;

    private Integer stock;

    private String status;
}