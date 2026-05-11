package com.cinemax.backend.service.promotion;

import com.cinemax.backend.model.dto.promotion.PromotionRequestDTO;
import com.cinemax.backend.model.dto.promotion.PromotionResponseDTO;

public interface PromotionService {
    PromotionResponseDTO calculatePromotion(PromotionRequestDTO request);
}