package com.cinemax.backend.service.venue;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cinemax.backend.model.dto.venue.VenueDropdownDTO;
import com.cinemax.backend.model.dto.venue.VenueRequestDTO;
import com.cinemax.backend.model.dto.venue.VenueResponseDTO;
import com.cinemax.backend.model.entity.District;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.DistrictRepository;
import com.cinemax.backend.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final DistrictRepository districtRepository;

    @Override
    public List<VenueResponseDTO> getAllVenues() {
        List<Venue> sedes = venueRepository.findAll();
        List<VenueResponseDTO> listaResponse = new ArrayList<>();

        for (Venue sede : sedes) {
            VenueResponseDTO dto = new VenueResponseDTO();
            dto.setIdVenue(sede.getIdVenue());
            dto.setNameVenue(sede.getNameVenue());
            dto.setAddressVenue(sede.getAddress());
            dto.setPhoneNumber(sede.getPhoneNumber());
            dto.setStatus(sede.getStatus());

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
            listaResponse.add(dto);
        }
        return listaResponse;
    }

    @Override
    public VenueResponseDTO createVenue(VenueRequestDTO request) {
        District dist = districtRepository.findById(request.getIdDistrict())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        Venue nuevaSede = Venue.builder()
                .nameVenue(request.getNameVenue())
                .address(request.getAddressVenue())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus())
                .district(dist)
                .build();

        Venue guardada = venueRepository.save(nuevaSede);

        VenueResponseDTO response = new VenueResponseDTO();
        response.setIdVenue(guardada.getIdVenue());
        response.setNameVenue(guardada.getNameVenue());
        response.setAddressVenue(guardada.getAddress());
        response.setPhoneNumber(guardada.getPhoneNumber());
        response.setStatus(guardada.getStatus());
        
        response.setIdDistrict(dist.getIdDistrict());
        response.setDistrictName(dist.getNameDistrict());
        
        if (dist.getProvince() != null) {
            response.setIdProvince(dist.getProvince().getIdProvince());
            response.setProvinceName(dist.getProvince().getNameProvince());
            
            if (dist.getProvince().getDepartment() != null) {
                response.setIdDepartment(dist.getProvince().getDepartment().getIdDepartment());
                response.setDepartmentName(dist.getProvince().getDepartment().getNameDepartment());
            }
        }

        return response;
    }

    @Override
    public VenueResponseDTO updateVenue(Integer idVenue, VenueRequestDTO request) {
        Venue sedeExistente = venueRepository.findById(idVenue)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        District dist = districtRepository.findById(request.getIdDistrict())
                .orElseThrow(() -> new RuntimeException("Distrito no encontrado"));

        sedeExistente.setNameVenue(request.getNameVenue());
        sedeExistente.setAddress(request.getAddressVenue());
        sedeExistente.setPhoneNumber(request.getPhoneNumber());
        sedeExistente.setStatus(request.getStatus());
        sedeExistente.setDistrict(dist);

        Venue actualizada = venueRepository.save(sedeExistente);

        VenueResponseDTO response = new VenueResponseDTO();
        response.setIdVenue(actualizada.getIdVenue());
        response.setNameVenue(actualizada.getNameVenue());
        response.setAddressVenue(actualizada.getAddress());
        response.setPhoneNumber(actualizada.getPhoneNumber());
        response.setStatus(actualizada.getStatus());
        
        response.setIdDistrict(dist.getIdDistrict());
        response.setDistrictName(dist.getNameDistrict());
        
        if (dist.getProvince() != null) {
            response.setIdProvince(dist.getProvince().getIdProvince());
            response.setProvinceName(dist.getProvince().getNameProvince());
            
            if (dist.getProvince().getDepartment() != null) {
                response.setIdDepartment(dist.getProvince().getDepartment().getIdDepartment());
                response.setDepartmentName(dist.getProvince().getDepartment().getNameDepartment());
            }
        }

        return response;
    }

    @Override
    public void deleteVenue(Integer idVenue) {
        if (!venueRepository.existsById(idVenue)) {
            throw new RuntimeException("La sede no existe");
        }
        venueRepository.deleteById(idVenue);
    }

    @Override
    public List<VenueDropdownDTO> getVenuesWithoutRole(Integer roleId) {
        List<Venue> availableVenues = venueRepository.findVenuesWithoutSpecificRole(roleId);
        List<VenueDropdownDTO> responseList = new ArrayList<>();
        for (Venue venue : availableVenues) {
            VenueDropdownDTO dto = new VenueDropdownDTO(
                    venue.getIdVenue(),
                    venue.getNameVenue()
            );
            
            responseList.add(dto);
        }
        return responseList;
    }
}