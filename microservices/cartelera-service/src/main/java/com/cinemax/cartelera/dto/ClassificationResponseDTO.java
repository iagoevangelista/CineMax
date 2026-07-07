package com.cinemax.cartelera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponseDTO {

    private String idClassification;

    private String nameClassification;

    private String descriptionText;
}