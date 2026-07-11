package com.cinemax.facturacion.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleTransactionRequestDTO {

    private Integer idShowtime;
    private List<Integer> asientosIds;
    private List<TicketLineDTO> tickets;
    private List<SnackLineDTO> snacks;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String paymentMethod;

    private Integer idPromotion;

    @Data
    public static class TicketLineDTO {
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