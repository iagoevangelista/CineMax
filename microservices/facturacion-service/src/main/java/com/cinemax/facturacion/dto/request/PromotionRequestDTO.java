package com.cinemax.facturacion.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionRequestDTO {
    private String idShowtime;
    private BigDecimal subtotal;
    private String promotionCode;
}