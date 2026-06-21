package com.cinemax.backend.model.entity;

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
@Table(name = "snack_venue_stock")
public class SnackVenueStock {

    @Id
    @Column(name = "id_snack_venue_stock")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSnackVenueStock;

    @ManyToOne
    @JoinColumn(name = "id_snack", nullable = false)
    private Snack snack;

    @ManyToOne
    @JoinColumn(name = "id_venue", nullable = false)
    private Venue venue;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "Activo";
}