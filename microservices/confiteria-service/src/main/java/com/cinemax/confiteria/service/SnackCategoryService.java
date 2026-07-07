package com.cinemax.confiteria.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cinemax.confiteria.dto.SnackCategoryRequestDTO;
import com.cinemax.confiteria.dto.SnackCategoryResponseDTO;
import com.cinemax.confiteria.entity.SnackCategory;
import com.cinemax.confiteria.repository.SnackCategoryRepository;

@Service
public class SnackCategoryService {

    @Autowired
    private SnackCategoryRepository snackCategoryRepository;

    public List<SnackCategoryResponseDTO> findAll() {
        return snackCategoryRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SnackCategoryResponseDTO findById(Integer id) {
        SnackCategory category = snackCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        return toResponseDTO(category);
    }

    public SnackCategoryResponseDTO create(SnackCategoryRequestDTO dto) {
        if (snackCategoryRepository.existsByNameCategory(dto.getNameCategory())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        SnackCategory category = SnackCategory.builder()
                .nameCategory(dto.getNameCategory())
                .build();

        return toResponseDTO(snackCategoryRepository.save(category));
    }

    public SnackCategoryResponseDTO update(Integer id, SnackCategoryRequestDTO dto) {
        SnackCategory category = snackCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        category.setNameCategory(dto.getNameCategory());

        return toResponseDTO(snackCategoryRepository.save(category));
    }

    public void delete(Integer id) {
        if (!snackCategoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con id: " + id);
        }
        snackCategoryRepository.deleteById(id);
    }

    private SnackCategoryResponseDTO toResponseDTO(SnackCategory category) {
        return SnackCategoryResponseDTO.builder()
                .idSnackCategory(category.getIdSnackCategory())
                .nameCategory(category.getNameCategory())
                .build();
    }
}