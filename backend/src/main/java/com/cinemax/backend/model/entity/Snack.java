package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "snack")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Snack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_snack")
    private Integer idSnack;

    @Column(name = "name_snack", nullable = false, length = 100)
    private String nameSnack;

    @Column(name = "description_snack", length = 250)
    private String descriptionSnack;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "image_url_snack", length = 250)
    private String imageUrlSnack;

    @Column(name = "status",length = 20)
    private String status = "Activo";


}
