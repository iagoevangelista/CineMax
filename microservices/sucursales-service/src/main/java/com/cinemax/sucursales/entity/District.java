package com.cinemax.sucursales.entity;

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
@Table(name = "district")
public class District {

    @Id
    @Column(name = "id_district")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDistrict;

    @Column(name = "name_district", nullable = false, length = 100)
    private String nameDistrict;

    @ManyToOne
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;
}