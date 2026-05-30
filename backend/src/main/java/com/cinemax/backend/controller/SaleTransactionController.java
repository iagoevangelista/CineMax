package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionRequestDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionResponseDTO;
import com.cinemax.backend.service.saletransaction.SaleTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/sale-transactions")
@RequiredArgsConstructor
public class SaleTransactionController {

    private final SaleTransactionService saleTransactionService;

    /**
     * POST /api/v1/sale-transactions
     * Solo usuarios autenticados pueden crear una transacción.
     * El email del usuario se extrae del JWT (Authentication).
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createSaleTransaction(
            @RequestBody SaleTransactionRequestDTO request,
            Authentication auth) {
        try {
            String userEmail = auth.getName();
            SaleTransactionResponseDTO response = saleTransactionService.createSaleTransaction(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}