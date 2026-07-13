package com.cinemax.facturacion.service;

import com.cinemax.facturacion.dto.request.PromotionRequestDTO;
import com.cinemax.facturacion.dto.response.PromotionResponseDTO;

public interface PromotionService {
    PromotionResponseDTO calculatePromotion(PromotionRequestDTO request);
}
