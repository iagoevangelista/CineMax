package com.cinemax.backend.model.entity;

<<<<<<< HEAD
import java.math.BigDecimal;

=======
>>>>>>> appmod/java-upgrade-20260509084445
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
<<<<<<< HEAD
    @Id
    @Column(name = "id_seat_type")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeatType;

    @Column(name = "name_seat_type", nullable = false, unique = true, length = 30)
    private String nameSeatType;

    @Column(name = "extra_price", precision = 10, scale = 2)
    private BigDecimal extraPrice;
}
=======

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seat_type")
    private Integer idSeatType;

    @Column(name = "name_seat_type", nullable = false, length = 50)
    private String nameSeatType;
}
>>>>>>> appmod/java-upgrade-20260509084445
