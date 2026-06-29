package com.cinemax.backend.service.saletransaction;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionHistoryDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionRequestDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionResponseDTO;

import java.util.List;

public interface SaleTransactionService {
    SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request, String userEmail);
    List<SaleTransactionHistoryDTO> getMyPurchases(String userEmail);
}