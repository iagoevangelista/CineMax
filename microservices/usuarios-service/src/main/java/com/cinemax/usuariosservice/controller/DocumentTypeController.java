package com.cinemax.usuariosservice.controller;

import com.cinemax.usuariosservice.model.dto.document.DocumentTypeResponseDTO;
import com.cinemax.usuariosservice.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeRepository documentRepository;

    @GetMapping
    public ResponseEntity<List<DocumentTypeResponseDTO>> getAllDocuments() {
        List<DocumentTypeResponseDTO> docs = documentRepository.findAll().stream()
                .map(d -> new DocumentTypeResponseDTO(d.getIdDocType(), d.getDocName()))
                .toList();
        return ResponseEntity.ok(docs);
    }
}