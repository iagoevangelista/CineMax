package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovie_IdMovieAndStatus(Integer idMovie, String status);

    @Query("SELECT s FROM Showtime s WHERE s.room.venue.idVenue = :idVenue " +
           "AND s.showDate = :showDate AND s.status <> 'Cancelada' " +
           "ORDER BY s.room.nameRoom, s.startTime")
    List<Showtime> findByVenueAndDate(
            @Param("idVenue") Integer idVenue,
            @Param("showDate") LocalDate showDate);

    @Query("SELECT s FROM Showtime s WHERE s.room.venue.idVenue = :idVenue " +
           "AND s.showDate BETWEEN :from AND :to AND s.status <> 'Cancelada' " +
           "ORDER BY s.showDate, s.room.nameRoom, s.startTime")
    List<Showtime> findByVenueAndDateRange(
            @Param("idVenue") Integer idVenue,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // FIX: parámetros como LocalTime (no java.sql.Time) + CAST explícito
    // para que SQL Server no confunda la columna datetime con el parámetro time
    @Query(value =
            "SELECT COUNT(*) FROM showtime s " +
            "WHERE s.id_room = :idRoom " +
            "AND s.show_date = :showDate " +
            "AND s.status <> 'Cancelada' " +
            "AND s.id_showtime <> :excludeId " +
            "AND (CAST(s.start_time AS TIME) < CAST(:endTime AS TIME) " +
            " AND CAST(s.end_time   AS TIME) > CAST(:startTime AS TIME))",
            nativeQuery = true)
    Long countConflictingShowtimeExcluding(
            @Param("idRoom") Integer idRoom,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Integer excludeId);

    @Query(value =
            "SELECT COUNT(*) FROM showtime s " +
            "WHERE s.id_room = :idRoom " +
            "AND s.show_date = :showDate " +
            "AND s.status <> 'Cancelada' " +
            "AND (CAST(s.start_time AS TIME) < CAST(:endTime AS TIME) " +
            " AND CAST(s.end_time   AS TIME) > CAST(:startTime AS TIME))",
            nativeQuery = true)
    Long countConflictingShowtime(
            @Param("idRoom") Integer idRoom,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}