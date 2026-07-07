package com.cinemax.cartelera.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cinemax.cartelera.entity.Classification;

public interface ClassificationRepository extends MongoRepository<Classification, String> {

    boolean existsByNameClassification(String nameClassification);
}