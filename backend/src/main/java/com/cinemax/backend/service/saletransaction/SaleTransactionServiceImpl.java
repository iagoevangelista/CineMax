package com.cinemax.backend.service.saletransaction;

import com.cinemax.backend.model.dto.saletransaction.SaleTransactionHistoryDTO;
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
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public SaleTransactionResponseDTO createSaleTransaction(SaleTransactionRequestDTO request, String userEmail) {

        // ── 1. Resolver usuario ───────────────────────────────────────────────
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // ── 2. Resolver showtime (opcional) ───────────────────────────────────
        Showtime showtime = null;
        if (request.getIdShowtime() != null) {
            showtime = showtimeRepository.findById(request.getIdShowtime())
                    .orElseThrow(() -> new RuntimeException("Función no encontrada."));

            if (!"Programada".equals(showtime.getStatus())) {
                throw new RuntimeException("Esta función ya no está disponible para reservas.");
            }
        }

        // ── 3. Validar asientos (solo si hay showtime) ────────────────────────
        List<Seat> seats = new ArrayList<>();
        if (showtime != null && request.getAsientosIds() != null) {
            for (Integer idSeat : request.getAsientosIds()) {
                Seat seat = seatRepository.findById(idSeat)
                        .orElseThrow(() -> new RuntimeException("Asiento ID " + idSeat + " no encontrado."));

                if ("MANTENIMIENTO".equals(seat.getStatus())) {
                    throw new RuntimeException("El asiento " + seat.getRowName() + seat.getColumnNumber()
                            + " está en mantenimiento y no puede reservarse.");
                }

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
        }

        // ── 4. Resolver Promotion (opcional) ──────────────────────────────────
        Promotion promotion = null;
        if (request.getIdPromotion() != null) {
            promotion = promotionRepository.findById(request.getIdPromotion()).orElse(null);
        }

        // ── 5. Resolver TransactionStatus "COMPLETADO" ────────────────────────
        TransactionStatus statusCompletado = transactionStatusRepository
                .findByNameStatus("COMPLETADO")
                .orElseThrow(() -> new RuntimeException("Estado 'COMPLETADO' no está configurado en la BD."));

        // ── 6. Crear SaleTransaction ──────────────────────────────────────────
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

        // ── 7. Crear SaleTicketDetail (solo si hay showtime y tickets) ─────────
        if (showtime != null && request.getTickets() != null && !request.getTickets().isEmpty()) {
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
        }

        // ── 8. Crear SaleSnackDetail y reducir stock ───────────────────────────
        if (request.getSnacks() != null) {
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
        }

        // ── 9. Reducir availableSeats (solo si hay showtime) ──────────────────
        if (showtime != null && !seats.isEmpty()) {
            showtime.setAvailableSeats(showtime.getAvailableSeats() - seats.size());
            showtimeRepository.save(showtime);
        }

        return new SaleTransactionResponseDTO(
                saleTransaction.getIdTransaction(),
                qrCodeData,
                "Reserva confirmada exitosamente."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleTransactionHistoryDTO> getMyPurchases(String userEmail) {
        UserAccount userAccount = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<SaleTransaction> transactions = saleTransactionRepository
                .findByUserAccount_IdUserOrderByPaymentDateDesc(userAccount.getIdUser());

        return transactions.stream().map(tx -> {
            SaleTransactionHistoryDTO dto = new SaleTransactionHistoryDTO();
            dto.setIdTransaction(tx.getIdTransaction());
            dto.setTotalAmount(tx.getTotalAmount());
            dto.setStatus(tx.getTransactionStatus().getNameStatus());

            // Tickets (película)
            List<SaleTicketDetail> tickets = saleTicketDetailRepository
                    .findBySaleTransaction_IdTransaction(tx.getIdTransaction());

            if (!tickets.isEmpty()) {
                SaleTicketDetail primerTicket = tickets.get(0);
                Showtime showtime = primerTicket.getShowtime();
                dto.setMovieTitle(showtime.getMovie().getTitleMovie());
                dto.setVenueName(showtime.getRoom().getVenue().getNameVenue());
                dto.setRoomName(showtime.getRoom().getNameRoom());
                dto.setDate(showtime.getShowDate().toString());
                dto.setTime(showtime.getStartTime().toString());
                dto.setSeats(tickets.stream()
                        .map(t -> t.getSeat().getRowName() + t.getSeat().getColumnNumber())
                        .collect(Collectors.joining(", ")));
            } else {
                dto.setMovieTitle(null);
                dto.setVenueName(null);
                dto.setRoomName(null);
                dto.setDate(tx.getPaymentDate().toLocalDate().toString());
                dto.setTime(tx.getPaymentDate().toLocalTime().toString());
                dto.setSeats(null);
            }

            // Snacks
            List<SaleSnackDetail> snackDetails = saleSnackDetailRepository
                    .findBySaleTransaction_IdTransaction(tx.getIdTransaction());

            dto.setSnacks(snackDetails.stream()
                    .map(s -> s.getQuantity() + "x " + s.getSnack().getNameSnack())
                    .collect(Collectors.toList()));

            return dto;
        }).collect(Collectors.toList());
    }
}