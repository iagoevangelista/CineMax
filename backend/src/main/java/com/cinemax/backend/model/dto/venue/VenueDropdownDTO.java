package com.cinemax.backend.model.dto.venue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor  
public class VenueDropdownDTO {
    private Integer idVenue;
    private String nameVenue;
}