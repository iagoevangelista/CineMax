package com.cinemax.backend.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_room")
    private Integer idRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venue", nullable = false)
    private Venue venue;

    @Column(name = "name_room", nullable = false, length = 50)
    private String nameRoom;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "status", length = 20)
    private String status = "Activa";

}
