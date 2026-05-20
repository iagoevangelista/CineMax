package com.cinemax.backend.model.dto.venue;

import lombok.Data;

@Data
public class VenueRequestDTO {
    private String nameVenue;
    private String addressVenue;
    private String phoneNumber;
    private String status;
    private Integer idDepartment;
    private Integer idProvince;
    private Integer idDistrict;
    private String imageUrl;
}