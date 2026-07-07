package com.cinemax.usuariosservice.model.dto.venue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueDTO {
    private Integer idVenue;
    private String nameVenue;
    private String status;
}