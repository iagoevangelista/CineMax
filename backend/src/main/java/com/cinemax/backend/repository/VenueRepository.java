package com.cinemax.backend.repository;


import com.cinemax.backend.model.entity.Venue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue,Integer> {
    @Query("SELECT v FROM Venue v WHERE v.status = 'Activo' AND NOT EXISTS (" + "SELECT u FROM UserAccount u WHERE u.venue = v AND u.role.idRole = :roleId AND u.status = 'Activo')")
    List<Venue> findVenuesWithoutSpecificRole(@Param("roleId") Integer roleId);
}
