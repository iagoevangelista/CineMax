package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Seat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByRoom_IdRoom(Integer idRoom);
    List<Seat> findByRoom_IdRoomOrderByRowNameAscColumnNumberAsc(Integer idRoom);
}
