package com.cinemax.backend.model.dto.venue;

import lombok.Data;

@Data
public class VenueResponseDTO {
    private Integer idVenue;
    private String nameVenue;
    private String addressVenue;
    private String phoneNumber;
    private String status;
    private String departmentName;
    private String provinceName;
    private String districtName;
    private Integer idDepartment;
    private Integer idProvince;
    private Integer idDistrict;
    private String imageUrl;
}