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

}