package com.cinemax.sucursales.repository;

import com.cinemax.sucursales.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    boolean existsByNameRoomAndVenue_IdVenue(String nameRoom, Integer idVenue);

    List<Room> findByVenue_IdVenue(Integer idVenue);
}