// model/entity/SaleTransaction.java
@Entity
@Table(name = "sale_transaction")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SaleTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Integer idTransaction;

    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @ManyToOne
    @JoinColumn(name = "id_transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "qr_code_data", length = 250)
    private String qrCodeData;
}