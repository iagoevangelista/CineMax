package com.cinemax.backend.model.entity;

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
@Table(name = "seat", uniqueConstraints = {
    @UniqueConstraint(name = "uq_seat_position", columnNames = {"id_room", "row_letter", "column_number"})
})
public class Seat {
    
    @Id
    @Column(name = "id_seat")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeat;

    @Column(name = "row_letter", nullable = false, length = 5)
    private String rowLetter;

    @Column(name = "column_number", nullable = false)
    private Integer columnNumber;

    @Builder.Default    
    @Column(name = "status", length = 20)
    @Pattern(regexp = "Disponible|Mantenimiento", message = "El estado debe ser 'Disponible' o 'Mantenimiento'")
    private String status = "Disponible";

    @ManyToOne
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "id_seat_type", nullable = false)
    private SeatType seatType;
}
