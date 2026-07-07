// dto/response/SaleTransactionResponseDTO.java
@Data @Builder
public class SaleTransactionResponseDTO {
    private Integer idTransaction;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String qrCodeData;
}