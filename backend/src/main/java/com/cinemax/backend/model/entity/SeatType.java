package com.cinemax.backend.model.entity;

import java.math.BigDecimal;

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
@Table(name = "seat_type")
public class SeatType {
    @Id
    @Column(name = "id_seat_type")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeatType;

    @Column(name = "name_seat_type", nullable = false, unique = true, length = 30)
    private String nameSeatType;

    @Column(name = "extra_price", precision = 10, scale = 2)
    private BigDecimal extraPrice;
}
