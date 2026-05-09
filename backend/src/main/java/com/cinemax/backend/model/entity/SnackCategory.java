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
@Table(name = "snack_category")
public class SnackCategory {
    @Id
    @Column(name = "id_snack_category")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSnackCategory;

    @Column(name = "name_category", nullable = false, unique = true, length = 50)
    private String nameCategory;
}
