package com.cinemax.backend.controller;

import com.cinemax.backend.model.entity.Classification;
import com.cinemax.backend.service.classification.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/classifications")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @GetMapping
    public ResponseEntity<List<Classification>> getAllClassifications() {
        return ResponseEntity.ok(classificationService.getAllClassifications());
    }
}