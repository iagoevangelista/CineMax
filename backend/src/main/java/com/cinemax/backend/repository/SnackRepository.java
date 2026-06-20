package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SnackRepository extends JpaRepository<Snack, Integer> {
    List<Snack> findByVenue_IdVenue(Integer idVenue);
}