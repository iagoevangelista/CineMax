package com.cinemax.facturacion.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotion_type")
public class PromotionType {
    @Id
    @Column(name = "id_promotion_type")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPromotionType;

    @Column(name = "name_promotion_type", nullable = false, unique = true, length = 50)
    private String namePromotionType;
}
