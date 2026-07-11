package com.cinemax.facturacion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaleTransactionResponseDTO {
    private Integer idTransaction;
    private String qrCodeData;
    private String message;
}