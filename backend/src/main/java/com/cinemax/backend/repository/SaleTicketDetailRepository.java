package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SaleTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleTicketDetailRepository extends JpaRepository<SaleTicketDetail, Integer> {
    List<SaleTicketDetail> findByShowtime_IdShowtime(Integer idShowtime);
    List<SaleTicketDetail> findBySaleTransaction_IdTransaction(Integer idTransaction);
}