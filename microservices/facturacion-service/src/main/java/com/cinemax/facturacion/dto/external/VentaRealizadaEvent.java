package com.cinemax.facturacion.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaRealizadaEvent {
    private Integer idTransaction;
    private Integer idUser;
    private BigDecimal totalAmount;
    private LocalDateTime fecha = LocalDateTime.now();

    public VentaRealizadaEvent(Integer idTransaction, Integer idUser, BigDecimal totalAmount) {
        this.idTransaction = idTransaction;
        this.idUser = idUser;
        this.totalAmount = totalAmount;
        this.fecha = LocalDateTime.now();
    }
}