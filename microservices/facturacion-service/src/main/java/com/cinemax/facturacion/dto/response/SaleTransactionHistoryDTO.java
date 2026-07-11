package com.cinemax.facturacion.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleTransactionHistoryDTO {
    private Integer idTransaction;
    private BigDecimal totalAmount;
    private String status;
    private String movieTitle;
    private String venueName;
    private String roomName;
    private String date;
    private String time;
    private String seats;
    private List<String> snacks;
}