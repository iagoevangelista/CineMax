package com.cinemax.sucursales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Versión mínima para poblar dropdowns/selects en el frontend, sin traer todos los campos de VenueResponseDTO.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueDropdownDTO {
    private Integer idVenue;
    private String nameVenue;
}