package com.cinemax.backend.model.dto.saletransaction;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleTransactionRequestDTO {

    private Integer idShowtime;

    /** IDs de los asientos seleccionados (uno por ticket) */
    private List<Integer> asientosIds;

    /** Desglose de tickets por categoría */
    private List<TicketLineDTO> tickets;

    /** Snacks elegidos (vacío si se omitió confitería) */
    private List<SnackLineDTO> snacks;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    /** "TARJETA" o "YAPE" */
    private String paymentMethod;

    /** null si no se aplicó ninguna promo */
    private Integer idPromotion;

    // ── Clases internas ──────────────────────────────────────────────────────

    @Data
    public static class TicketLineDTO {
        /** "ADULTO", "NINO", "ADULTO_MAYOR", "DISCAPACITADO" */
        private String categoryCode;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }

    @Data
    public static class SnackLineDTO {
        private Integer idSnack;
        private Integer cantidad;
        private BigDecimal unitPrice;
    }
}