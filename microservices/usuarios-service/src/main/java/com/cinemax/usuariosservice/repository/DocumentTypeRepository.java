package com.cinemax.usuariosservice.repository;

import java.util.Optional;
import com.cinemax.usuariosservice.model.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {
    Optional<DocumentType> findByDocName(String docName);
}