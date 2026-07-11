package com.cinemax.facturacion.dto.external;

import lombok.Data;

/**
 * Copia local de lo que necesitamos de cartelera-service, no el DTO completo
 * de ese servicio. Se llena con GET /api/showtimes/{id}.
 *
 * NOTA PENDIENTE DE EQUIPO: cartelera-service usa MongoDB y expone idShowtime
 * como String (ObjectId). El frontend, hoy, manda idShowtime como number.
 * Mientras el equipo no resincronice esto, seguimos tratándolo como Integer
 * porque es lo que realmente llega desde el Angular.
 */
@Data
public class ShowtimeDTO {
    private Integer idShowtime;
    private String movieTitle;
    private Integer idRoom;
    private String status;
    private String showDate;
    private String startTime;
}