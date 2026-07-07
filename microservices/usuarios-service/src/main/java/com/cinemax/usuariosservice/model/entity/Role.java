package com.cinemax.usuariosservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "role")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Integer idRole;

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "id_role"),
        inverseJoinColumns = @JoinColumn(name = "id_permission")
    )
    private Set<Permission> permissions;
}