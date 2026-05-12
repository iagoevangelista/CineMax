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
@Table(name = "province")
public class Province {
    @Id
    @Column(name = "id_province")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProvince;

    @Column(name = "name_province", nullable = false, length = 100)
    private String nameProvince;

    @ManyToOne
    @JoinColumn(name = "id_department", nullable = false)
    private Department department;
}
