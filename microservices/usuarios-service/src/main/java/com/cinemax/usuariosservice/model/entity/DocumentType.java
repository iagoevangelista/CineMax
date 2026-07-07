package com.cinemax.usuariosservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doc_type")
    private Integer idDocType;

    @Column(name = "doc_name", length = 50, nullable = false)
    private String docName;
}