package com.cinemax.backend.model.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "venue")
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Venue {


}
