package com.cinemax.facturacion.controller;

import com.cinemax.facturacion.dto.request.SaleTransactionRequestDTO;
import com.cinemax.facturacion.dto.response.SaleTransactionHistoryDTO;
import com.cinemax.facturacion.dto.response.SaleTransactionResponseDTO;
import com.cinemax.facturacion.service.SaleTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sale-transactions")
@RequiredArgsConstructor
public class SaleTransactionController {

    private final SaleTransactionService saleTransactionService;

    // No recibimos el JWT aquí: el service lo resuelve internamente llamando a
    // UsuariosClient.getProfile(), que reenvía el header Authorization (ver FeignConfig).
    // Las excepciones de negocio las captura GlobalExceptionHandler.

    @PostMapping
    public SaleTransactionResponseDTO createSaleTransaction(@Valid @RequestBody SaleTransactionRequestDTO request) {
        return saleTransactionService.createSaleTransaction(request);
    }

    @GetMapping("/my-purchases")
    public List<SaleTransactionHistoryDTO> getMyPurchases() {
        return saleTransactionService.getMyPurchases();
    }
}