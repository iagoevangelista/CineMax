package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Promotion {

    @Id
    @Column(name = "id_promotion")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPromotion;

    @Column(name = "name_promotion", nullable = false, length = 100)
    private String namePromotion;

    @Column(name = "type_promotion", nullable = false, length = 50)
    private String typePromotion;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;

    @Column(name = "status",length = 20)
    private String status = "Activo";

}
