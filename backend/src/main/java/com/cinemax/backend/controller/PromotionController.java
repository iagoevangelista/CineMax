package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.promotion.PromotionRequestDTO;
import com.cinemax.backend.model.dto.promotion.PromotionResponseDTO;
import com.cinemax.backend.service.promotion.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/calculate")
    public ResponseEntity<PromotionResponseDTO> calculatePromotion(@RequestBody PromotionRequestDTO request) {
        return ResponseEntity.ok(promotionService.calculatePromotion(request));
    }
}