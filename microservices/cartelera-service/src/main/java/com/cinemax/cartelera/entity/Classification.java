package com.cinemax.cartelera.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "classifications")
public class Classification {

    @Id
    private String idClassification;

    private String nameClassification;

    private String descriptionText;
}