package com.cinemax.sucursales.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room")
public class Room {

    @Id
    @Column(name = "id_room")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRoom;

    @Column(name = "name_room", nullable = false, length = 50)
    private String nameRoom;

    @Positive(message = "Capacidad debe ser un entero positivo")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "num_rows")
    private Integer numRows;

    @Column(name = "seats_per_row")
    private Integer seatsPerRow;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Pattern(regexp = "Activo|Inactivo", message = "Status must be 'Activo' or 'Inactivo'")
    private String status = "Activo";

    @ManyToOne
    @JoinColumn(name = "id_venue", nullable = false)
    private Venue venue;
}