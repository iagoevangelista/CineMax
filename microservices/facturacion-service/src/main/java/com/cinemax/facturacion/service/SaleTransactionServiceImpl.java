// service/SaleTransactionServiceImpl.java
@Service
@RequiredArgsConstructor
public class SaleTransactionServiceImpl implements SaleTransactionService {

    private final SaleTransactionRepository saleTransactionRepository;
    private final SaleTicketDetailRepository saleTicketDetailRepository;
    private final SaleSnackDetailRepository saleSnackDetailRepository;
    private final TransactionStatusRepository transactionStatusRepository;

    private final CarteleraClient carteleraClient;
    private final SucursalesClient sucursalesClient;
    private final DulceriaClient dulceriaClient;

    private final VentaEventPublisher ventaEventPublisher;

    @Override
    @Transactional
    public SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request, Integer idUser) {

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SaleTicketDetail> ticketDetails = new ArrayList<>();
        List<SaleSnackDetail> snackDetails = new ArrayList<>();

        // Procesar tickets: validar showtime (cartelera) y reservar asiento (sucursales)
        if (request.getTickets() != null) {
            for (var item : request.getTickets()) {

                ShowtimeDTO showtime = carteleraClient.getShowtime(item.getIdShowtime());
                if (showtime == null) {
                    throw new IllegalStateException("La función indicada no existe.");
                }

                boolean reservado = sucursalesClient.reservarAsiento(
                        new ReservarAsientoRequestDTO(item.getIdShowtime(), item.getIdSeat())
                );
                if (!reservado) {
                    throw new IllegalStateException("El asiento ya está ocupado o no existe.");
                }

                BigDecimal ticketPrice = showtime.getBaseTicketPrice();
                subtotal = subtotal.add(ticketPrice);

                ticketDetails.add(SaleTicketDetail.builder()
                        .idShowtime(item.getIdShowtime())
                        .idSeat(item.getIdSeat())
                        .ticketPrice(ticketPrice)
                        .build());
            }
        }

        // Procesar snacks: descontar stock (dulcería)
        if (request.getSnacks() != null) {
            for (var item : request.getSnacks()) {

                DescontarStockResponseDTO stockResp = dulceriaClient.descontarStock(
                        new DescontarStockRequestDTO(item.getIdSnack(), item.getQuantity())
                );
                if (!stockResp.isSuccess()) {
                    throw new IllegalStateException("Stock insuficiente para el snack solicitado.");
                }

                BigDecimal lineTotal = stockResp.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(lineTotal);

                snackDetails.add(SaleSnackDetail.builder()
                        .idSnack(item.getIdSnack())
                        .quantity(item.getQuantity())
                        .unitPrice(stockResp.getUnitPrice())
                        .build());
            }
        }

        // Calcular totales
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        TransactionStatus statusPendiente = transactionStatusRepository.findByNameStatus("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("Estado PENDIENTE no configurado."));

        // Guardar
        SaleTransaction transaction = SaleTransaction.builder()
                .idUser(idUser)
                .transactionStatus(statusPendiente)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .build();

        transaction = saleTransactionRepository.save(transaction);

        for (var t : ticketDetails) t.setSaleTransaction(transaction);
        for (var s : snackDetails) s.setSaleTransaction(transaction);
        saleTicketDetailRepository.saveAll(ticketDetails);
        saleSnackDetailRepository.saveAll(snackDetails);

        // Publicar evento asíncrono
        ventaEventPublisher.publicarVentaRealizada(
                new VentaRealizadaEvent(transaction.getIdTransaction(), idUser, totalAmount)
        );

        return SaleTransactionResponseDTO.builder()
                .idTransaction(transaction.getIdTransaction())
                .status(statusPendiente.getNameStatus())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .paymentMethod(transaction.getPaymentMethod())
                .createdAt(transaction.getCreatedAt())
                .qrCodeData(transaction.getQrCodeData())
                .build();
    }
}