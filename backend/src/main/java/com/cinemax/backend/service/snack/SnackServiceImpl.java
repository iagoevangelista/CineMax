package com.cinemax.backend.service.snack;

import com.cinemax.backend.model.dto.snack.SnackRequestDTO;
import com.cinemax.backend.model.dto.snack.SnackResponseDTO;
import com.cinemax.backend.model.entity.Snack;
import com.cinemax.backend.model.entity.SnackCategory;
import com.cinemax.backend.model.entity.SnackVenueStock;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.SnackCategoryRepository;
import com.cinemax.backend.repository.SnackRepository;
import com.cinemax.backend.repository.SnackVenueStockRepository;
import com.cinemax.backend.repository.VenueRepository;

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
    private final SnackVenueStockRepository snackVenueStockRepository;
    private final VenueRepository venueRepository;

    // IDs de los snacks predeterminados (lista base que toda sede tiene)
    private static final List<Integer> SNACKS_PREDETERMINADOS = List.of(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
    );

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

    @Override
    public List<SnackResponseDTO> getSnacksByVenue(Integer idVenue) {
        List<SnackVenueStock> registros = snackVenueStockRepository
                .findByVenue_IdVenueAndStockGreaterThan(idVenue, 0).stream()
                .filter(svs -> "Activo".equals(svs.getStatus()))
                .toList();

        // Si la sede no tiene snacks asignados, devolver solo la lista predeterminada (ids 1-18)
        if (registros.isEmpty()) {
            return snackRepository.findAllById(SNACKS_PREDETERMINADOS).stream()
                    .filter(s -> "Activo".equals(s.getStatus()))
                    .map(this::convertToDTO)
                    .toList();
        }

        return registros.stream()
                .map(svs -> {
                    SnackResponseDTO dto = convertToDTO(svs.getSnack());
                    dto.setStock(svs.getStock());
                    dto.setStatus(svs.getStatus());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<SnackResponseDTO> getSnacksByVenueAdmin(Integer idVenue) {
        return snackVenueStockRepository.findByVenue_IdVenue(idVenue).stream()
                .map(svs -> {
                    SnackResponseDTO dto = convertToDTO(svs.getSnack());
                    dto.setStock(svs.getStock());
                    dto.setStatus(svs.getStatus());
                    return dto;
                })
                .toList();
    }

    @Override
    public void inhabilitarSnackEnSede(Integer idSnack, Integer idVenue) {
        SnackVenueStock svs = snackVenueStockRepository
                .findBySnack_IdSnackAndVenue_IdVenue(idSnack, idVenue)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado en esta sede"));
        svs.setStatus("Inactivo");
        snackVenueStockRepository.save(svs);
    }

    @Override
    public void habilitarSnackEnSede(Integer idSnack, Integer idVenue) {
        SnackVenueStock svs = snackVenueStockRepository
                .findBySnack_IdSnackAndVenue_IdVenue(idSnack, idVenue)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado en esta sede"));
        svs.setStatus("Activo");
        snackVenueStockRepository.save(svs);
    }

    @Override
    public void actualizarStockEnSede(Integer idSnack, Integer idVenue, Integer stock) {
        SnackVenueStock svs = snackVenueStockRepository
                .findBySnack_IdSnackAndVenue_IdVenue(idSnack, idVenue)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado en esta sede"));
        svs.setStock(stock);
        snackVenueStockRepository.save(svs);
    }

    @Override
    public void agregarSnackASede(Integer idSnack, Integer idVenue, Integer stock) {
        Snack snack = snackRepository.findById(idSnack)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado"));
        Venue venue = venueRepository.findById(idVenue)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));
        SnackVenueStock svs = SnackVenueStock.builder()
                .snack(snack).venue(venue).stock(stock).status("Activo").build();
        snackVenueStockRepository.save(svs);
    }

    @Override
    public void agregarSnackATodasLasSedes(Integer idSnack, Integer stock) {
        Snack snack = snackRepository.findById(idSnack)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado"));
        for (Venue venue : venueRepository.findAll()) {
            SnackVenueStock svs = SnackVenueStock.builder()
                    .snack(snack).venue(venue).stock(stock).status("Activo").build();
            snackVenueStockRepository.save(svs);
        }
    }

    @Override
    public int contarSedesConSnack(Integer idSnack) {
        return snackVenueStockRepository.findAll().stream()
                .filter(svs -> svs.getSnack().getIdSnack().equals(idSnack))
                .toList().size();
    }

    @Override
    public void eliminarSnackDeSede(Integer idSnack, Integer idVenue) {
        SnackVenueStock svs = snackVenueStockRepository
                .findBySnack_IdSnackAndVenue_IdVenue(idSnack, idVenue)
                .orElseThrow(() -> new RuntimeException("Snack no encontrado en esta sede"));
        snackVenueStockRepository.delete(svs);
    }

    @Override
    public void eliminarSnackDeTodo(Integer idSnack) {
        List<SnackVenueStock> registros = snackVenueStockRepository.findAll().stream()
                .filter(svs -> svs.getSnack().getIdSnack().equals(idSnack))
                .toList();
        snackVenueStockRepository.deleteAll(registros);
        snackRepository.deleteById(idSnack);
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