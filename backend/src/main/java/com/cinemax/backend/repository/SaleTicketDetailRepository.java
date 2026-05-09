package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.SaleTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleTicketDetailRepository extends JpaRepository<SaleTicketDetail,Integer> {
}
