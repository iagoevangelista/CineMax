package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_ticket_detail", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ticket_seat_showtime", columnNames = {"id_showtime", "id_seat"})
})
public class SaleTicketDetail {

    @Id
    @Column(name = "id_ticket")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTicket;

    @ManyToOne
    @JoinColumn(name = "id_transaction", nullable = false)
    private SaleTransaction saleTransaction;

    @ManyToOne
    @JoinColumn(name = "id_showtime", nullable = false)
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "id_seat", nullable = false)
    private Seat seat;

    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Builder.Default
    @Column(name = "is_used")
    private Boolean isUsed = false;
}
