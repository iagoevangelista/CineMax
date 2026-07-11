package com.cinemax.facturacion.service;

import com.cinemax.facturacion.dto.request.SaleTransactionRequestDTO;
import com.cinemax.facturacion.dto.response.SaleTransactionHistoryDTO;
import com.cinemax.facturacion.dto.response.SaleTransactionResponseDTO;

import java.util.List;

public interface SaleTransactionService {
    SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request);
    List<SaleTransactionHistoryDTO> getMyPurchases();
}