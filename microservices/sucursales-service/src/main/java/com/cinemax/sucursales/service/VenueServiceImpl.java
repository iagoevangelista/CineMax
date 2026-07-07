package com.cinemax.sucursales.service;

import com.cinemax.common.cloudinary.CloudinaryService;
import com.cinemax.sucursales.dto.VenueDropdownDTO;
import com.cinemax.sucursales.dto.VenueRequestDTO;
import com.cinemax.sucursales.dto.VenueResponseDTO;
import com.cinemax.sucursales.entity.District;
import com.cinemax.sucursales.entity.Venue;
import com.cinemax.sucursales.repository.DistrictRepository;
import com.cinemax.sucursales.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final DistrictRepository districtRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<VenueResponseDTO> getAllVenues() {
        List<Venue> sedes = venueRepository.findAll();
        List<VenueResponseDTO> listaResponse = new ArrayList<>();
        for (Venue sede : sedes) {
            listaResponse.add(toResponseDTO(sede));
        }
        return listaResponse;
    }

    @Override
    public List<VenueResponseDTO> getActiveVenues() {
        return venueRepository.findByStatus("Activo").stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<VenueDropdownDTO> getActiveVenuesForDropdown() {
        return venueRepository.findByStatus("Activo").stream()
                .map(v -> new VenueDropdownDTO(v.getIdVenue(), v.getNameVenue()))
                .toList();
    }

    @Override
    public VenueResponseDTO getVenueById(Integer id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sede no encontrada con id: " + id));
        return toResponseDTO(venue);
    }

    @Override
    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO request, MultipartFile image) {
        District dist = districtRepository.findById(request.getIdDistrict())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(image);
            } catch (IOException e) {
                throw new RuntimeException("Error al subir la imagen a Cloudinary", e);
            }
        }

        Venue venue = Venue.builder()
                .nameVenue(request.getNameVenue())
                .address(request.getAddressVenue())
                .phoneNumber(request.getPhoneNumber())
                .district(dist)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(imageUrl)
                .build();

        Venue guardada = venueRepository.save(venue);
        return toResponseDTO(guardada);
    }

    @Override
    @Transactional
    public VenueResponseDTO updateVenue(Integer id, VenueRequestDTO request, MultipartFile image) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sede no encontrada con id: " + id));

        District dist = districtRepository.findById(request.getIdDistrict())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        venue.setNameVenue(request.getNameVenue());
        venue.setAddress(request.getAddressVenue());
        venue.setPhoneNumber(request.getPhoneNumber());
        venue.setDistrict(dist);
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());

        // El frontend real cambia el estado (Activo/Inactivo) a través de este mismo PUT, no del DELETE físico
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            venue.setStatus(request.getStatus());
        }

        if (image != null && !image.isEmpty()) {
            try {
                venue.setImageUrl(cloudinaryService.uploadImage(image));
            } catch (IOException e) {
                throw new RuntimeException("Error al subir la imagen a Cloudinary", e);
            }
        }

        return toResponseDTO(venueRepository.save(venue));
    }

    @Override
    @Transactional
    public void deleteVenue(Integer id) {
        if (!venueRepository.existsById(id)) {
            throw new NoSuchElementException("La sede no existe con id: " + id);
        }
        venueRepository.deleteById(id);
    }

    private VenueResponseDTO toResponseDTO(Venue sede) {
        VenueResponseDTO dto = new VenueResponseDTO();
        dto.setIdVenue(sede.getIdVenue());
        dto.setNameVenue(sede.getNameVenue());
        dto.setAddressVenue(sede.getAddress());
        dto.setPhoneNumber(sede.getPhoneNumber());
        dto.setStatus(sede.getStatus());
        dto.setImageUrl(sede.getImageUrl());
        dto.setLatitude(sede.getLatitude());
        dto.setLongitude(sede.getLongitude());

        if (sede.getDistrict() != null) {
            dto.setDistrictName(sede.getDistrict().getNameDistrict());
            dto.setIdDistrict(sede.getDistrict().getIdDistrict());

            if (sede.getDistrict().getProvince() != null) {
                dto.setProvinceName(sede.getDistrict().getProvince().getNameProvince());
                dto.setIdProvince(sede.getDistrict().getProvince().getIdProvince());

                if (sede.getDistrict().getProvince().getDepartment() != null) {
                    dto.setDepartmentName(sede.getDistrict().getProvince().getDepartment().getNameDepartment());
                    dto.setIdDepartment(sede.getDistrict().getProvince().getDepartment().getIdDepartment());
                }
            }
        }
        return dto;
    }
}