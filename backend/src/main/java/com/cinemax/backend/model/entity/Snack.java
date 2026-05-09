package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "snack")
public class Snack {

    @Id
    @Column(name = "id_snack")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSnack;

    @ManyToOne
    @JoinColumn(name = "id_snack_category", nullable = false)
    private SnackCategory snackCategory;

    @Column(name = "name_snack", nullable = false, length = 100)
    private String nameSnack;

    @Column(name = "description_snack", length = 250)
    private String descriptionSnack;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Column(name = "image_url_snack", length = 250)
    private String imageUrlSnack;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Pattern(regexp = "Activo|Inactivo", message = "El estado debe ser 'Activo' o 'Inactivo'")
    private String status = "Activo";

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
