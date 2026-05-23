package com.cinemax.backend.model.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RoomRequestDTO {
    @NotBlank(message = "El nombre de la sala es obligatorio")
    private String nameRoom;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser un número positivo")
    private Integer capacity;

    @NotNull(message = "Debe definir la cantidad de filas")
    private Integer numRows;

    @NotNull(message = "Debe definir la cantidad de asientos por fila")
    private Integer seatsPerRow;

    private String status;

    @NotNull(message = "Debe seleccionar a qué sede pertenece la sala")
    private Integer idVenue;
}