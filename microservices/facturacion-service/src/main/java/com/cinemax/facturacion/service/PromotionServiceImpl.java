package com.cinemax.facturacion.service;

import com.cinemax.facturacion.client.CarteleraClient;
import com.cinemax.facturacion.dto.external.ShowtimeDTO;
import com.cinemax.facturacion.dto.request.PromotionRequestDTO;
import com.cinemax.facturacion.dto.response.PromotionResponseDTO;
import com.cinemax.facturacion.model.entity.Promotion;
import com.cinemax.facturacion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CarteleraClient carteleraClient;

    @Override
    public PromotionResponseDTO calculatePromotion(PromotionRequestDTO request) {
        // Antes (monolito): join directo a Showtime en la misma BD.
        // Ahora: Showtime vive en cartelera-service (Mongo), se resuelve vía Feign.
        ShowtimeDTO funcion = carteleraClient.getShowtime(request.getIdShowtime());
        if (funcion == null) {
            throw new RuntimeException("Horario no encontrado");
        }

        LocalDate fechaFuncion = LocalDate.parse(funcion.getShowDate());
        String diaSemana = fechaFuncion.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        List<Promotion> activas = promotionRepository.findByStatus("Activo");

        Promotion mejorPromocion = null;
        BigDecimal mayorDescuento = BigDecimal.ZERO;
        String mejorMotivoAplicado = "";

        for (Promotion promo : activas) {

            if (fechaFuncion.isBefore(promo.getStartDate()) || fechaFuncion.isAfter(promo.getEndDate())) {
                continue;
            }

            String motivoActual;

            if (promo.getPromotionCode() != null && !promo.getPromotionCode().isEmpty()) {
                if (promo.getPromotionCode().equalsIgnoreCase(request.getPromotionCode())) {
                    motivoActual = "Código aplicado: " + promo.getPromotionCode();
                } else {
                    continue;
                }
            } else {
                if (promo.getDayOfWeek() != null && !promo.getDayOfWeek().isEmpty()) {
                    if (!promo.getDayOfWeek().toLowerCase().contains(diaSemana.toLowerCase())) {
                        continue;
                    }
                    motivoActual = "Promoción del día: " + promo.getNamePromotion();
                } else {
                    motivoActual = "Promoción global: " + promo.getNamePromotion();
                }
            }

            BigDecimal descuentoCalculado = BigDecimal.ZERO;

            if (promo.getDiscountPercentage() != null && promo.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentaje = promo.getDiscountPercentage().divide(new BigDecimal("100"));
                descuentoCalculado = request.getSubtotal().multiply(porcentaje);
            } else if (promo.getDiscountFixedAmount() != null) {
                descuentoCalculado = promo.getDiscountFixedAmount();
            }

            if (descuentoCalculado.compareTo(mayorDescuento) > 0) {
                mayorDescuento = descuentoCalculado;
                mejorPromocion = promo;
                mejorMotivoAplicado = motivoActual;
            }
        }

        PromotionResponseDTO response = new PromotionResponseDTO();
        response.setOriginalAmount(request.getSubtotal());

        if (mejorPromocion != null) {
            BigDecimal montoFinal = request.getSubtotal().subtract(mayorDescuento);
            if (montoFinal.compareTo(BigDecimal.ZERO) < 0) montoFinal = BigDecimal.ZERO;

            response.setIdPromotion(mejorPromocion.getIdPromotion());
            response.setNamePromotion(mejorMotivoAplicado);
            response.setDiscountAmount(mayorDescuento.setScale(2, RoundingMode.HALF_UP));
            response.setFinalAmount(montoFinal.setScale(2, RoundingMode.HALF_UP));
        } else {
            response.setNamePromotion("Sin promoción");
            response.setDiscountAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            response.setFinalAmount(request.getSubtotal().setScale(2, RoundingMode.HALF_UP));
        }

        return response;
    }
}
