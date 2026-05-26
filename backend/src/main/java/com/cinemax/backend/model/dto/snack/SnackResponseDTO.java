package com.cinemax.backend.model.dto.snack;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SnackResponseDTO {
    private Integer idSnack;
    private String nameSnack;
    private String descriptionSnack;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String imageUrlSnack;
    private Integer idSnackCategory;
    private String nameCategory;
}