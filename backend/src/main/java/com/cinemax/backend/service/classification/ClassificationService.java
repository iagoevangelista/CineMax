package com.cinemax.backend.service.classification;

import com.cinemax.backend.model.entity.Classification;
import java.util.List;

public interface ClassificationService {
    List<Classification> getAllClassifications();
}