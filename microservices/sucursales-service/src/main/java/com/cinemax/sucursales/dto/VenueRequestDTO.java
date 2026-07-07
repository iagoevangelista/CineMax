package com.cinemax.sucursales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VenueRequestDTO {

    @NotBlank(message = "El nombre de la sede es obligatorio.")
    private String nameVenue;

    @NotBlank(message = "La dirección de la sede es obligatoria.")
    private String addressVenue;

    private String phoneNumber;
    private String status;
    private Double latitude;
    private Double longitude;

    @NotNull(message = "Debe seleccionar un departamento.")
    private Integer idDepartment;

    @NotNull(message = "Debe seleccionar una provincia.")
    private Integer idProvince;

    @NotNull(message = "Debe seleccionar un distrito.")
    private Integer idDistrict;

    private String imageUrl;
}