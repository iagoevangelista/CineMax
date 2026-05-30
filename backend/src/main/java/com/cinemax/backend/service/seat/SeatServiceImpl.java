package com.cinemax.backend.service.seat;

import com.cinemax.backend.model.dto.seat.SeatStatusDTO;
import com.cinemax.backend.model.entity.SaleTicketDetail;
import com.cinemax.backend.model.entity.Seat;
import com.cinemax.backend.model.entity.Showtime;
import com.cinemax.backend.repository.SaleTicketDetailRepository;
import com.cinemax.backend.repository.SeatRepository;
import com.cinemax.backend.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final SaleTicketDetailRepository ticketDetailRepository;

    @Override
    public List<SeatStatusDTO> getSeatsStatusByShowtime(Integer idShowtime) {

        Showtime funcion = showtimeRepository.findById(idShowtime)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + idShowtime));

        List<Seat> todosLosAsientos = seatRepository.findByRoom_IdRoom(funcion.getRoom().getIdRoom());

        List<SaleTicketDetail> ticketsVendidos = ticketDetailRepository.findByShowtime_IdShowtime(idShowtime);

        Set<Integer> idsOcupados = new HashSet<>();
        for (SaleTicketDetail ticket : ticketsVendidos) {
            idsOcupados.add(ticket.getSeat().getIdSeat());
        }

        List<SeatStatusDTO> response = new ArrayList<>();

        for (Seat asiento : todosLosAsientos) {
            SeatStatusDTO dto = new SeatStatusDTO();
            dto.setIdSeat(asiento.getIdSeat());
            dto.setRowLetter(asiento.getRowName());
            dto.setColumnNumber(asiento.getColumnNumber());
            dto.setStatus(asiento.getStatus()); // "ACTIVO", "MANTENIMIENTO", "OCULTO"

            if (asiento.getSeatType() != null) {
                dto.setNameSeatType(asiento.getSeatType().getNameSeatType());
            }

            // Un asiento en MANTENIMIENTO u OCULTO nunca puede estar "ocupado" por venta,
            // pero se marca isOccupied=true para que el frontend lo trate como no seleccionable
            if ("MANTENIMIENTO".equals(asiento.getStatus()) || "OCULTO".equals(asiento.getStatus())) {
                dto.setIsOccupied(false); // OCULTO se omite en el frontend; MANTENIMIENTO se renderiza pero bloqueado
            } else {
                dto.setIsOccupied(idsOcupados.contains(asiento.getIdSeat()));
            }

            response.add(dto);
        }

        return response;
    }
}