package com.cinemax.facturacion.controller;

import com.cinemax.facturacion.dto.request.PromotionRequestDTO;
import com.cinemax.facturacion.dto.response.PromotionResponseDTO;
import com.cinemax.facturacion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/calculate")
    public ResponseEntity<PromotionResponseDTO> calculatePromotion(@RequestBody PromotionRequestDTO request) {
        return ResponseEntity.ok(promotionService.calculatePromotion(request));
    }
}
