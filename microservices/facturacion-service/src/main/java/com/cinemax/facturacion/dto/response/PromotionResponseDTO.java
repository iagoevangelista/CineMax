package com.cinemax.facturacion.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionResponseDTO {
    private Integer idPromotion;
    private String namePromotion;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}
