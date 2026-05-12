package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Movie;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByStatusAndIsActiveTrue(String status);
}
