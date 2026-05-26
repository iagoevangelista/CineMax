package com.cinemax.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinemax.backend.model.entity.SnackCategory;
import com.cinemax.backend.repository.SnackCategoryRepository;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/snack-categories")
@RequiredArgsConstructor
public class SnackCategoryController {

    private final SnackCategoryRepository snackCategoryRepository;

    @GetMapping
    public ResponseEntity<List<SnackCategory>> getAllCategories() {
        return ResponseEntity.ok(snackCategoryRepository.findAll());
    }
}