package com.cinemax.backend.service.saletransaction;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionRequestDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionResponseDTO;

public interface SaleTransactionService {
    SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request, String userEmail);
}