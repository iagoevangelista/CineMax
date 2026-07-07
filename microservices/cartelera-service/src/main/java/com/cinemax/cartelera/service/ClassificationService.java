package com.cinemax.cartelera.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cinemax.cartelera.dto.ClassificationRequestDTO;
import com.cinemax.cartelera.dto.ClassificationResponseDTO;
import com.cinemax.cartelera.entity.Classification;
import com.cinemax.cartelera.repository.ClassificationRepository;

@Service
public class ClassificationService {

    @Autowired
    private ClassificationRepository classificationRepository;

    public List<ClassificationResponseDTO> findAll() {
        return classificationRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ClassificationResponseDTO findById(String id) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clasificación no encontrada con id: " + id));
        return toResponseDTO(classification);
    }

    public ClassificationResponseDTO create(ClassificationRequestDTO dto) {
        if (classificationRepository.existsByNameClassification(dto.getNameClassification())) {
            throw new RuntimeException("Ya existe una clasificación con ese nombre");
        }

        Classification classification = Classification.builder()
                .nameClassification(dto.getNameClassification())
                .descriptionText(dto.getDescriptionText())
                .build();

        return toResponseDTO(classificationRepository.save(classification));
    }

    public ClassificationResponseDTO update(String id, ClassificationRequestDTO dto) {
        Classification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clasificación no encontrada con id: " + id));

        classification.setNameClassification(dto.getNameClassification());
        classification.setDescriptionText(dto.getDescriptionText());

        return toResponseDTO(classificationRepository.save(classification));
    }

    public void delete(String id) {
        if (!classificationRepository.existsById(id)) {
            throw new RuntimeException("Clasificación no encontrada con id: " + id);
        }
        classificationRepository.deleteById(id);
    }

    private ClassificationResponseDTO toResponseDTO(Classification classification) {
        return ClassificationResponseDTO.builder()
                .idClassification(classification.getIdClassification())
                .nameClassification(classification.getNameClassification())
                .descriptionText(classification.getDescriptionText())
                .build();
    }
}