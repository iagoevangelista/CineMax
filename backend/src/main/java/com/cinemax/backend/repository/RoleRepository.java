package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {


    Optional<Role> findByRoleName(String roleName);
}
