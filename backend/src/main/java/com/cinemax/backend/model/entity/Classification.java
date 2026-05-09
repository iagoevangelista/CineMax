package com.cinemax.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;   
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classification")
public class Classification {
    
    @Id
    @Column(name = "id_classification")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idClassification;

    @Column(name = "name_classification", nullable = false, unique = true, length = 30)
    private String nameClassification;

    @Column(name = "description_text", length = 255)
    private String descriptionText;
}
