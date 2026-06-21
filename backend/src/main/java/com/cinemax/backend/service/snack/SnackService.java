package com.cinemax.backend.service.snack;

import com.cinemax.backend.model.dto.snack.SnackRequestDTO;
import com.cinemax.backend.model.dto.snack.SnackResponseDTO;
import java.util.List;

public interface SnackService {
    List<SnackResponseDTO> getAllSnacks();
    SnackResponseDTO getSnackById(Integer id);
    SnackResponseDTO createSnack(SnackRequestDTO request, String imageUrl);
    SnackResponseDTO updateSnack(Integer id, SnackRequestDTO request, String imageUrl);
    void deleteSnack(Integer id);
    List<SnackResponseDTO> getSnacksByCategory(Integer idCategory);
    List<SnackResponseDTO> getSnacksByVenue(Integer idVenue);
    List<SnackResponseDTO> getSnacksByVenueAdmin(Integer idVenue);
    void inhabilitarSnackEnSede(Integer idSnack, Integer idVenue);
    void habilitarSnackEnSede(Integer idSnack, Integer idVenue);
    void actualizarStockEnSede(Integer idSnack, Integer idVenue, Integer stock);
    void agregarSnackASede(Integer idSnack, Integer idVenue, Integer stock);
    void agregarSnackATodasLasSedes(Integer idSnack, Integer stock);
    void eliminarSnackDeSede(Integer idSnack, Integer idVenue);
    void eliminarSnackDeTodo(Integer idSnack);
    int contarSedesConSnack(Integer idSnack);
}