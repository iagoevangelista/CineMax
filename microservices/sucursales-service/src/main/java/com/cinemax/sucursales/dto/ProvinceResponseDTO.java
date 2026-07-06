package com.cinemax.sucursales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvinceResponseDTO {
    private Integer idProvince;
    private String nameProvince;
    private Integer idDepartment;
}