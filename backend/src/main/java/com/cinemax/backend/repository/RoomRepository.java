package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // Método para validar que el nombre de la sala no se repita en la misma sede (HU-16)
    boolean existsByNameRoomAndVenue_IdVenue(String nameRoom, Integer idVenue);
}