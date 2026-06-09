package com.cinemax.backend.service.saletransaction;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionRequestDTO;
import com.cinemax.backend.model.dto.saletransaction.SaleTransactionResponseDTO;
import com.cinemax.backend.model.entity.*;
import com.cinemax.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleTransactionServiceImpl implements SaleTransactionService {

    private final SaleTransactionRepository     saleTransactionRepository;
    private final SaleTicketDetailRepository    saleTicketDetailRepository;
    private final SaleSnackDetailRepository     saleSnackDetailRepository;
    private final UserAccountRepository         userAccountRepository;
    private final ShowtimeRepository            showtimeRepository;
    private final SeatRepository                seatRepository;
    private final SnackRepository               snackRepository;
    private final PromotionRepository           promotionRepository;
    private final TransactionStatusRepository   transactionStatusRepository;

    /**
     * Crea una SaleTransaction completa:
     *  1. Valida asientos (que no estén ya en otro SaleTicketDetail para esa función).
     *  2. Crea el SaleTransaction con estado COMPLETADO.
     *  3. Crea un SaleTicketDetail por cada asiento, asignando el precio
     *     según la distribución de categorías enviada por el frontend.
     *  4. Crea un SaleSnackDetail por cada snack y reduce su stock.
     *  5. Reduce availableSeats del Showtime.
     *  6. Retorna el idTransaction y un qrCodeData único.
     */
    @Override
    @Transactional
    public SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request, String userEmail) {

        // ── 1. Resolver entidades base ────────────────────────────────────────
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Showtime showtime = showtimeRepository.findById(request.getIdShowtime())
                .orElseThrow(() -> new RuntimeException("Función no encontrada."));

        if (!"Programada".equals(showtime.getStatus())) {
            throw new RuntimeException("Esta función ya no está disponible para reservas.");
        }

        // ── 2. Validar asientos ───────────────────────────────────────────────
        List<Seat> seats = new ArrayList<>();
        for (Integer idSeat : request.getAsientosIds()) {
            Seat seat = seatRepository.findById(idSeat)
                    .orElseThrow(() -> new RuntimeException("Asiento ID " + idSeat + " no encontrado."));

            if ("MANTENIMIENTO".equals(seat.getStatus())) {
                throw new RuntimeException("El asiento " + seat.getRowName() + seat.getColumnNumber()
                        + " está en mantenimiento y no puede reservarse.");
            }

            // Verificar que no exista ya un SaleTicketDetail para este asiento en esta función
            boolean yaOcupado = saleTicketDetailRepository
                    .findByShowtime_IdShowtime(request.getIdShowtime())
                    .stream()
                    .anyMatch(td -> td.getSeat().getIdSeat().equals(idSeat));

            if (yaOcupado) {
                throw new RuntimeException("El asiento " + seat.getRowName() + seat.getColumnNumber()
                        + " ya fue reservado. Por favor elige otro.");
            }

            seats.add(seat);
        }

        // ── 3. Resolver Promotion (opcional) ─────────────────────────────────
        Promotion promotion = null;
        if (request.getIdPromotion() != null) {
            promotion = promotionRepository.findById(request.getIdPromotion()).orElse(null);
        }

        // ── 4. Resolver TransactionStatus "COMPLETADO" ────────────────────────
        TransactionStatus statusCompletado = transactionStatusRepository
                .findByNameStatus("COMPLETADO")
                .orElseThrow(() -> new RuntimeException("Estado 'COMPLETADO' no está configurado en la BD."));

        // ── 5. Crear SaleTransaction ──────────────────────────────────────────
        BigDecimal discountAmount = request.getDiscountAmount() != null
                ? request.getDiscountAmount() : BigDecimal.ZERO;

        String qrCodeData = "TXN-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();

        SaleTransaction saleTransaction = SaleTransaction.builder()
                .userAccount(userAccount)
                .promotion(promotion)
                .transactionStatus(statusCompletado)
                .subtotal(request.getSubtotal())
                .discountAmount(discountAmount)
                .totalAmount(request.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .qrCodeData(qrCodeData)
                .build();

        saleTransaction = saleTransactionRepository.save(saleTransaction);

        // ── 6. Crear SaleTicketDetail (1 por asiento) ─────────────────────────
        //
        // El frontend manda tickets agrupados por categoría con su precioUnitario.
        // Expandimos esa lista a precios individuales y los asignamos en orden
        // a cada asiento. La constraint uq_ticket_seat_showtime evita duplicados.
        //
        List<BigDecimal> preciosPorAsiento = new ArrayList<>();
        for (SaleTransactionRequestDTO.TicketLineDTO linea : request.getTickets()) {
            for (int i = 0; i < linea.getCantidad(); i++) {
                preciosPorAsiento.add(linea.getPrecioUnitario());
            }
        }

        if (preciosPorAsiento.size() != seats.size()) {
            throw new RuntimeException("La cantidad de tickets no coincide con la cantidad de asientos seleccionados.");
        }

        for (int i = 0; i < seats.size(); i++) {
            SaleTicketDetail saleTicketDetail = SaleTicketDetail.builder()
                    .saleTransaction(saleTransaction)
                    .showtime(showtime)
                    .seat(seats.get(i))
                    .ticketPrice(preciosPorAsiento.get(i))
                    .isUsed(false)
                    .build();
            saleTicketDetailRepository.save(saleTicketDetail);
        }

        // ── 7. Crear SaleSnackDetail y reducir stock ───────────────────────────
        for (SaleTransactionRequestDTO.SnackLineDTO linea : request.getSnacks()) {
            Snack snack = snackRepository.findById(linea.getIdSnack())
                    .orElseThrow(() -> new RuntimeException("Snack ID " + linea.getIdSnack() + " no encontrado."));

            if (snack.getStock() < linea.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + snack.getNameSnack());
            }

            SaleSnackDetail saleSnackDetail = SaleSnackDetail.builder()
                    .saleTransaction(saleTransaction)
                    .snack(snack)
                    .quantity(linea.getCantidad())
                    .unitPrice(linea.getUnitPrice())
                    .isDelivered(false)
                    .build();
            saleSnackDetailRepository.save(saleSnackDetail);

            snack.setStock(snack.getStock() - linea.getCantidad());
            snackRepository.save(snack);
        }

        // ── 8. Reducir availableSeats del Showtime ────────────────────────────
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seats.size());
        showtimeRepository.save(showtime);

        return new SaleTransactionResponseDTO(
                saleTransaction.getIdTransaction(),
                qrCodeData,
                "Reserva confirmada exitosamente."
        );
    }
}