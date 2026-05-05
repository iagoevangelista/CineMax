package com.cinemax.backend.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seat")
    private Integer idSeat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;

    @Column(name = "row_letter", nullable = false, length = 5)
    private String rowLetter;

    @Column(name = "column_number", nullable = false)
    private Integer columnNumber;

    @Column(name = "status", length = 20)
    private String status = "Disponible";

}
