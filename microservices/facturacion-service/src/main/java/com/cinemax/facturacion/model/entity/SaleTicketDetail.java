// model/entity/SaleTicketDetail.java
@Entity
@Table(name = "sale_ticket_detail", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ticket_seat_showtime", columnNames = {"id_showtime", "id_seat"})
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SaleTicketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Integer idTicket;

    @ManyToOne
    @JoinColumn(name = "id_transaction", nullable = false)
    private SaleTransaction saleTransaction;

    @Column(name = "id_showtime", nullable = false)
    private Integer idShowtime;

    @Column(name = "id_seat", nullable = false)
    private Integer idSeat;

    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Builder.Default
    @Column(name = "is_used")
    private Boolean isUsed = false;
}