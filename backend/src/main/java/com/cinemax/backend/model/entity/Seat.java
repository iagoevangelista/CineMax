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

    @Column(name = "row_name", length = 5, nullable = false)
    private String rowName;

    @Column(name = "column_number", nullable = false)
    private Integer columnNumber; // Ej: 1, 2, 3

    // ESTADO FÍSICO: "ACTIVO", "MANTENIMIENTO", "OCULTO"
    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "ACTIVO";

    // TIPO: "REGULAR", "WHEELCHAIR"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seat_type", nullable = false)
    private SeatType seatType;

    @ManyToOne
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;


}