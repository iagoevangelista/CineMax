package com.cinemax.backend.repository;

import com.cinemax.backend.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    // Spring Data JPA crea automáticamente la consulta SQL por debajo:
    // SELECT * FROM user_account WHERE email = ?
    Optional<UserAccount> findByEmail(String email);
    boolean existsByRole_IdRoleAndStatus(Integer idRole, String status);
    boolean existsByEmail(String email);
    boolean existsByRole_IdRoleAndVenue_IdVenueAndStatus(Integer idRole, Integer idVenue, String status);
    Optional<UserAccount> findByRole_IdRoleAndStatus(Integer idRole, String status);
    Optional<UserAccount> findByRole_IdRoleAndVenue_IdVenueAndStatus(Integer idRole, Integer idVenue, String status);
    Optional<UserAccount> findByResetToken(String resetToken);
}