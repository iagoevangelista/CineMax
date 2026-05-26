package com.cinemax.backend.model.dto.snack;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SnackRequestDTO {
    private String nameSnack;
    private String descriptionSnack;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private Integer idSnackCategory;
}