package com.cinemax.backend.model.dto.saletransaction;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleTransactionHistoryDTO {
    private Integer idTransaction;
    private String movieTitle;
    private String venueName;
    private String roomName;
    private String date;
    private String time;
    private String seats;
    private BigDecimal totalAmount;
    private String status;
    private List<String> snacks;
}