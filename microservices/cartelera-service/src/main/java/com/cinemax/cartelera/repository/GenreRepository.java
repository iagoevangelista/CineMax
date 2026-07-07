package com.cinemax.cartelera.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cinemax.cartelera.entity.Genre;

public interface GenreRepository extends MongoRepository<Genre, String> {

    boolean existsByNameGenre(String nameGenre);
}