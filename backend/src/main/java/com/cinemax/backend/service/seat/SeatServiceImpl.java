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
            dto.setNameSeatType(asiento.getSeatType());
            
            if (idsOcupados.contains(asiento.getIdSeat())) {
                dto.setIsOccupied(true);
            } else {
                dto.setIsOccupied(false);
            }
            
            response.add(dto);
        }
        
        return response;
    }
}