// dto/request/SaleTransactionRequestDTO.java
@Data
public class SaleTransactionRequestDTO {

    @NotNull
    private String paymentMethod;

    private List<TicketItemDTO> tickets;   // puede venir vacío si solo compra snacks
    private List<SnackItemDTO> snacks;     // puede venir vacío si solo compra entradas

    @Data
    public static class TicketItemDTO {
        @NotNull private Integer idShowtime;
        @NotNull private Integer idSeat;
    }

    @Data
    public static class SnackItemDTO {
        @NotNull private Integer idSnack;
        @Positive private Integer quantity;
    }
}