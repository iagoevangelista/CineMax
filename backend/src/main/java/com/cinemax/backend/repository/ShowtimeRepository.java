package com.cinemax.backend.repository;


import com.cinemax.backend.model.entity.Showtime;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime,Integer> {
    List<Showtime> findByMovie_IdMovieAndStatus(Integer idMovie, String status);
}
