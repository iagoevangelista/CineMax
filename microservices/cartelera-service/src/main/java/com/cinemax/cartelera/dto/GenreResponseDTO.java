package com.cinemax.cartelera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponseDTO {

    private String idGenre;

    private String nameGenre;
}