package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_snack_detail")
public class SaleSnackDetail {

    @Id
    @Column(name = "id_detail")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetail;

    @ManyToOne
    @JoinColumn(name = "id_transaction", nullable = false)
    private SaleTransaction saleTransaction;

    @ManyToOne
    @JoinColumn(name = "id_snack", nullable = false)
    private Snack snack;

    @Column(name = "quantity", nullable = false)
    @Positive
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Builder.Default
    @Column(name = "is_delivered")
    private Boolean isDelivered = false;
}
