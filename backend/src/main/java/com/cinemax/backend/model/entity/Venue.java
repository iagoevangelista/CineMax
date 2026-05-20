package com.cinemax.backend.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venue")
public class Venue {
    @Id
    @Column(name = "id_venue")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenue;

    @Column(name = "name_venue", nullable = false, length = 100)
    private String nameVenue;

    @Column(name = "address", nullable = false, length = 150)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Pattern(regexp = "Activo|Inactivo", message = "Status must be either 'Activo' or 'Inactivo'")
    private String status = "Activo";

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Añade esto debajo de tus otros atributos
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "id_district", nullable = false)
    private District district;
    

}
