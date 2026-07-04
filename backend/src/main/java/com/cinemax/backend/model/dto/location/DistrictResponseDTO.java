package com.cinemax.backend.model.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistrictResponseDTO {
    private Integer idDistrict;
    private String nameDistrict;
    private Integer idProvince;
}