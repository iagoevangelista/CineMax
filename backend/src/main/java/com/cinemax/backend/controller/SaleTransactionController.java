package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionHistoryDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionRequestDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionResponseDTO;
import com.cinemax.backend.service.saletransaction.SaleTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/sale-transactions")
@RequiredArgsConstructor
public class SaleTransactionController {

    private final SaleTransactionService saleTransactionService;

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

    @GetMapping("/my-purchases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPurchases(Authentication auth) {
        try {
            String userEmail = auth.getName();
            List<SaleTransactionHistoryDTO> purchases = saleTransactionService.getMyPurchases(userEmail);
            return ResponseEntity.ok(purchases);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}