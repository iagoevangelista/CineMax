package com.cinemax.backend.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Classification {

    @Id
    @Column(name = "classification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer classificationId;

    @Column(name = "name_classification", nullable = false, unique = true, length = 20)
    private String nameClassification;

    @Column(name = "description_text", length = 255)
    private String descriptionText;


}
