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
@Table(name = "promotion_scope")
public class PromotionScope {

    @Id
    @Column(name = "id_scope")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idScope;

    @ManyToOne
    @JoinColumn(name = "id_promotion", nullable = false)
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "id_movie")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "id_genre")
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "id_snack")
    private Snack snack;

    @ManyToOne
    @JoinColumn(name = "id_venue")
    private Venue venue;

    @PrePersist
    @PreUpdate
    private void validateSingleScope() {
        int count = 0;
        if (movie != null) count++;
        if (genre != null) count++;
        if (snack != null) count++;
        if (venue != null) count++;

        if (count != 1) {
            throw new IllegalStateException("Error de integridad: La promoción debe aplicar exactamente a un solo alcance (Película, Género, Snack o Sede).");
        }
    }
}