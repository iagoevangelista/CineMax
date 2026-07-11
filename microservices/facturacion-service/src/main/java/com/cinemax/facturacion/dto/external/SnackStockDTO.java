package com.cinemax.facturacion.dto.external;

import lombok.Data;

/**
 * Copia local del contrato real de confiteria-service (SnackVenueStockResponseDTO /
 * SnackVenueStockRequestDTO). idSnackVenueStock es el ID del REGISTRO de stock
 * (no el idSnack del producto) - es el que hay que usar en el PUT.
 */
@Data
public class SnackStockDTO {
    private Integer idSnackVenueStock;
    private Integer snackId;
    private String snackName;
    private Integer idVenue;
    private Integer stock;
    private String status;
}