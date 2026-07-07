// model/entity/TransactionStatus.java
@Entity
@Table(name = "transaction_status")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction_status")
    private Integer idTransactionStatus;

    @Column(name = "name_status", nullable = false, unique = true, length = 30)
    private String nameStatus;
}