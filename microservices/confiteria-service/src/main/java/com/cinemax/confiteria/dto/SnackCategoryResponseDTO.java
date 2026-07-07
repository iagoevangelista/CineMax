package com.cinemax.confiteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackCategoryResponseDTO {

    private Integer idSnackCategory;

    private String nameCategory;
}