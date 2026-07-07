package com.cinemax.usuariosservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permission")
    private Integer idPermission;

    @Column(name = "permission_name", length = 50, nullable = false, unique = true)
    private String permissionName;
}