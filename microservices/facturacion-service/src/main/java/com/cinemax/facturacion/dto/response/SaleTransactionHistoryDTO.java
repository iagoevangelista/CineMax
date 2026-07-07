// dto/response/SaleTransactionHistoryDTO.java
@Data @Builder
public class SaleTransactionHistoryDTO {
    private Integer idTransaction;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<TicketDetailDTO> tickets;
    private List<SnackDetailDTO> snacks;

    @Data @Builder
    public static class TicketDetailDTO {
        private Integer idShowtime;
        private Integer idSeat;
        private BigDecimal ticketPrice;
    }

    @Data @Builder
    public static class SnackDetailDTO {
        private Integer idSnack;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}