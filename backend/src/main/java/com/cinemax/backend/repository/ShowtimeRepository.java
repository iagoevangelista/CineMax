package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Showtime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {
    
    List<Showtime> findByMovie_IdMovieAndStatus(Integer idMovie, String status);

    // NUEVO: Verifica si hay un cruce de horarios en una sala específica y en una fecha exacta
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Showtime s " +
            "WHERE s.room.idRoom = :idRoom " +
            "AND s.showDate = :showDate " +
            "AND s.status <> 'Cancelada' " +
            "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    boolean existsConflictingShowtime(
            @Param("idRoom") Integer idRoom,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}