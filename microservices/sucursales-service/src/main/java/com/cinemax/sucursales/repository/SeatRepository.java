package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findByRoom_IdRoom(Integer idRoom);
    List<Seat> findByRoom_IdRoomOrderByRowNameAscColumnNumberAsc(Integer idRoom);
}