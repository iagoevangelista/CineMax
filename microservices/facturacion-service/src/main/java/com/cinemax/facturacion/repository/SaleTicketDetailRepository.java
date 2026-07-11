package com.cinemax.facturacion.repository;

import com.cinemax.facturacion.model.entity.SaleTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleTicketDetailRepository extends JpaRepository<SaleTicketDetail, Integer> {
    List<SaleTicketDetail> findBySaleTransaction_IdTransaction(Integer idTransaction);
    boolean existsByIdShowtimeAndIdSeat(Integer idShowtime, Integer idSeat);
}