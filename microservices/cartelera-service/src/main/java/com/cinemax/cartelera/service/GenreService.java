package com.cinemax.cartelera.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cinemax.cartelera.dto.GenreRequestDTO;
import com.cinemax.cartelera.dto.GenreResponseDTO;
import com.cinemax.cartelera.entity.Genre;
import com.cinemax.cartelera.repository.GenreRepository;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public List<GenreResponseDTO> findAll() {
        return genreRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public GenreResponseDTO findById(String id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Género no encontrado con id: " + id));
        return toResponseDTO(genre);
    }

    public GenreResponseDTO create(GenreRequestDTO dto) {
        if (genreRepository.existsByNameGenre(dto.getNameGenre())) {
            throw new RuntimeException("Ya existe un género con ese nombre");
        }

        Genre genre = Genre.builder()
                .nameGenre(dto.getNameGenre())
                .build();

        return toResponseDTO(genreRepository.save(genre));
    }

    public GenreResponseDTO update(String id, GenreRequestDTO dto) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Género no encontrado con id: " + id));

        genre.setNameGenre(dto.getNameGenre());

        return toResponseDTO(genreRepository.save(genre));
    }

    public void delete(String id) {
        if (!genreRepository.existsById(id)) {
            throw new RuntimeException("Género no encontrado con id: " + id);
        }
        genreRepository.deleteById(id);
    }

    private GenreResponseDTO toResponseDTO(Genre genre) {
        return GenreResponseDTO.builder()
                .idGenre(genre.getIdGenre())
                .nameGenre(genre.getNameGenre())
                .build();
    }
}