package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_type")
public class DocumentType {
    
    @Id
    @Column(name = "id_document_type")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDocumentType;

    @Column(name = "name_document_type", nullable = false, unique = true, length = 30)
    private String nameDocumentType;

}
