package com.cinemax.backend.service.genre;

import com.cinemax.backend.model.entity.Genre;
import java.util.List;

public interface GenreService {
    List<Genre> getAllGenres();
}