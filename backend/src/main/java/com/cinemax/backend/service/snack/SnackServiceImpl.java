package com.cinemax.backend.service.snack;

import com.cinemax.backend.model.dto.snack.SnackRequestDTO;
import com.cinemax.backend.model.dto.snack.SnackResponseDTO;
import com.cinemax.backend.model.entity.Snack;
import com.cinemax.backend.model.entity.SnackCategory;
import com.cinemax.backend.repository.SnackCategoryRepository;
import com.cinemax.backend.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SnackServiceImpl implements SnackService {

    private final SnackRepository snackRepository;
    private final SnackCategoryRepository snackCategoryRepository;

    @Override
    public List<SnackResponseDTO> getAllSnacks() {
        return snackRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public SnackResponseDTO getSnackById(Integer id) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado con ID: " + id));
        return convertToDTO(snack);
    }

    @Override
    public SnackResponseDTO createSnack(SnackRequestDTO request, String imageUrl) {
        SnackCategory category = snackCategoryRepository.findById(request.getIdSnackCategory())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Snack snack = Snack.builder()
                .nameSnack(request.getNameSnack())
                .descriptionSnack(request.getDescriptionSnack())
                .price(request.getPrice())
                .stock(request.getStock())
                .status(request.getStatus() != null ? request.getStatus() : "Activo")
                .imageUrlSnack(imageUrl)
                .snackCategory(category)
                .build();

        return convertToDTO(snackRepository.save(snack));
    }

    @Override
    public SnackResponseDTO updateSnack(Integer id, SnackRequestDTO request, String imageUrl) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado con ID: " + id));

        SnackCategory category = snackCategoryRepository.findById(request.getIdSnackCategory())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        snack.setNameSnack(request.getNameSnack());
        snack.setDescriptionSnack(request.getDescriptionSnack());
        snack.setPrice(request.getPrice());
        snack.setStock(request.getStock());
        snack.setStatus(request.getStatus());
        snack.setSnackCategory(category);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            snack.setImageUrlSnack(imageUrl);
        }

        return convertToDTO(snackRepository.save(snack));
    }

    @Override
    public void deleteSnack(Integer id) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado con ID: " + id));
        snack.setStatus("Inactivo");
        snackRepository.save(snack);
    }

    @Override
    public List<SnackResponseDTO> getSnacksByCategory(Integer idCategory) {
        return snackRepository.findAll().stream()
                .filter(s -> s.getSnackCategory().getIdSnackCategory().equals(idCategory))
                .map(this::convertToDTO)
                .toList();
    }

    private SnackResponseDTO convertToDTO(Snack snack) {
        SnackResponseDTO dto = new SnackResponseDTO();
        dto.setIdSnack(snack.getIdSnack());
        dto.setNameSnack(snack.getNameSnack());
        dto.setDescriptionSnack(snack.getDescriptionSnack());
        dto.setPrice(snack.getPrice());
        dto.setStock(snack.getStock());
        dto.setStatus(snack.getStatus());
        dto.setImageUrlSnack(snack.getImageUrlSnack());
        if (snack.getSnackCategory() != null) {
            dto.setIdSnackCategory(snack.getSnackCategory().getIdSnackCategory());
            dto.setNameCategory(snack.getSnackCategory().getNameCategory());
        }
        return dto;
    }
}