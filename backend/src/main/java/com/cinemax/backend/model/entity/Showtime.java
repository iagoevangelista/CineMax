package com.cinemax.backend.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "showtime")
public class Showtime {
    @Id
    @Column(name = "id_showtime")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idShowtime;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "language_format", nullable = false, length = 50)
    private String languageFormat;

    
    @Column(name = "base_ticket_price", nullable = false, precision = 10, scale = 2)
    @Min(value = 0, message = "El precio base no puede ser negativo")
    private BigDecimal baseTicketPrice;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Pattern(regexp = "Programada|En Curso|Finalizada|Cancelada", message = "El estado debe ser: Programada, En Curso, Finalizada o Cancelada")
    private String status = "Programada";

    @ManyToOne
    @JoinColumn(name = "id_movie", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "id_room", nullable = false)
    private Room room;
}