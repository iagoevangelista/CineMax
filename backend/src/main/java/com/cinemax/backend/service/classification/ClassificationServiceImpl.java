package com.cinemax.backend.service.classification;

import com.cinemax.backend.model.entity.Classification;
import com.cinemax.backend.repository.ClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    private final ClassificationRepository classificationRepository;

    @Override
    public List<Classification> getAllClassifications() {
        return classificationRepository.findAll();
    }
}