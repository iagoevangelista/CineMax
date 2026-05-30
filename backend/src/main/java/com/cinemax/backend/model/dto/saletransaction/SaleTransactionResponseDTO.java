package com.cinemax.backend.model.dto.saletransaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaleTransactionResponseDTO {
    private Integer idTransaction;
    private String  qrCodeData;
    private String  message;
}