package com.cinemax.usuariosservice.model.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponseDTO {
    private Integer idDocType;
    private String docName;
}