package com.cinemax.cartelera.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cinemax.cartelera.entity.Movie;

public interface MovieRepository extends MongoRepository<Movie, String> {

    boolean existsByTitleMovie(String titleMovie);

    List<Movie> findByStatus(String status);

    List<Movie> findByIsActiveTrue();
}