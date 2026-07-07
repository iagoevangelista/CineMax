package com.cinemax.sucursales.service;

import com.cinemax.sucursales.dto.VenueDropdownDTO;
import com.cinemax.sucursales.dto.VenueRequestDTO;
import com.cinemax.sucursales.dto.VenueResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VenueService {
    List<VenueResponseDTO> getAllVenues();
    List<VenueResponseDTO> getActiveVenues();
    List<VenueDropdownDTO> getActiveVenuesForDropdown();
    VenueResponseDTO getVenueById(Integer id);
    VenueResponseDTO createVenue(VenueRequestDTO request, MultipartFile image);
    VenueResponseDTO updateVenue(Integer id, VenueRequestDTO request, MultipartFile image);
    void deleteVenue(Integer id);
}