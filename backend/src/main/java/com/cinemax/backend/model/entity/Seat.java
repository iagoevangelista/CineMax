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
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seat")
    private Integer idSeat;

    @Column(name = "row_letter", length = 5, nullable = false) // Cambiado de row_name a row_letter
    private String rowName;

    @Column(name = "column_number", nullable = false)
    private Integer columnNumber; // Ej: 1, 2, 3

    // ESTADO FÍSICO: "ACTIVO", "MANTENIMIENTO", "OCULTO"
    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "ACTIVO";

    // TIPO: "REGULAR", "WHEELCHAIR"
    @Builder.Default
    @Column(name = "seat_type", length = 20)
    private String seatType = "REGULAR";

    @ManyToOne
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;
}