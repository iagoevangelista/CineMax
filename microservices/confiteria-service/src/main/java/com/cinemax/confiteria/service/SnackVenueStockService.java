package com.cinemax.confiteria.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cinemax.confiteria.dto.SnackVenueStockRequestDTO;
import com.cinemax.confiteria.dto.SnackVenueStockResponseDTO;
import com.cinemax.confiteria.entity.Snack;
import com.cinemax.confiteria.entity.SnackVenueStock;
import com.cinemax.confiteria.repository.SnackRepository;
import com.cinemax.confiteria.repository.SnackVenueStockRepository;

@Service
public class SnackVenueStockService {

    @Autowired
    private SnackVenueStockRepository snackVenueStockRepository;

    @Autowired
    private SnackRepository snackRepository;

    public List<SnackVenueStockResponseDTO> findAll() {
        return snackVenueStockRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SnackVenueStockResponseDTO> findByVenue(Integer idVenue) {
        return snackVenueStockRepository.findByIdVenue(idVenue)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<SnackVenueStockResponseDTO> findBySnack(Integer snackId) {
        return snackVenueStockRepository.findBySnack_IdSnack(snackId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SnackVenueStockResponseDTO findById(Integer id) {
        SnackVenueStock stock = snackVenueStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de stock no encontrado con id: " + id));
        return toResponseDTO(stock);
    }

    public SnackVenueStockResponseDTO create(SnackVenueStockRequestDTO dto) {
        Snack snack = snackRepository.findById(dto.getSnackId())
                .orElseThrow(() -> new RuntimeException("Snack no encontrado con id: " + dto.getSnackId()));

        boolean yaExiste = !snackVenueStockRepository
                .findByIdVenueAndSnack_IdSnack(dto.getIdVenue(), dto.getSnackId())
                .isEmpty();
        if (yaExiste) {
            throw new RuntimeException("Ya existe un registro de stock para este snack en esta sucursal");
        }

        SnackVenueStock stock = SnackVenueStock.builder()
                .snack(snack)
                .idVenue(dto.getIdVenue())
                .stock(dto.getStock())
                .status(dto.getStatus() != null ? dto.getStatus() : "Activo")
                .build();

        return toResponseDTO(snackVenueStockRepository.save(stock));
    }

    public SnackVenueStockResponseDTO update(Integer id, SnackVenueStockRequestDTO dto) {
        SnackVenueStock stock = snackVenueStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de stock no encontrado con id: " + id));

        if (dto.getSnackId() != null) {
            Snack snack = snackRepository.findById(dto.getSnackId())
                    .orElseThrow(() -> new RuntimeException("Snack no encontrado con id: " + dto.getSnackId()));
            stock.setSnack(snack);
        }

        stock.setIdVenue(dto.getIdVenue());
        stock.setStock(dto.getStock());

        if (dto.getStatus() != null) {
            stock.setStatus(dto.getStatus());
        }

        return toResponseDTO(snackVenueStockRepository.save(stock));
    }

    public void delete(Integer id) {
        if (!snackVenueStockRepository.existsById(id)) {
            throw new RuntimeException("Registro de stock no encontrado con id: " + id);
        }
        snackVenueStockRepository.deleteById(id);
    }

    private SnackVenueStockResponseDTO toResponseDTO(SnackVenueStock stock) {
        return SnackVenueStockResponseDTO.builder()
                .idSnackVenueStock(stock.getIdSnackVenueStock())
                .snackId(stock.getSnack().getIdSnack())
                .snackName(stock.getSnack().getNameSnack())
                .idVenue(stock.getIdVenue())
                .stock(stock.getStock())
                .status(stock.getStatus())
                .build();
    }
}