package com.cinemax.backend.service.venue;

import com.cinemax.backend.model.dto.venue.VenueDropdownDTO;
import com.cinemax.backend.model.dto.venue.VenueRequestDTO;
import com.cinemax.backend.model.dto.venue.VenueResponseDTO;

import java.util.List;

public interface VenueService {
    List<VenueResponseDTO> getAllVenues();
    VenueResponseDTO createVenue(VenueRequestDTO request);
    VenueResponseDTO updateVenue(Integer idVenue, VenueRequestDTO request);
    void deleteVenue(Integer idVenue);
    List<VenueDropdownDTO> getVenuesWithoutRole(Integer roleId);
}