package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.room.RoomRequestDTO;
import com.cinemax.backend.model.dto.room.RoomResponseDTO;
import com.cinemax.backend.service.room.RoomService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del RoomController (HU-16 - Gestión de Salas).
 *
 * Estrategia: llamada directa al método Java del Controller, sin levantar Spring.
 * Verifica que el Controller:
 *   - llama al RoomService con los parámetros correctos
 *   - devuelve el ResponseEntity con el status HTTP esperado
 *   - propaga correctamente las RuntimeException del Service
 *
 * Nota: @PreAuthorize se verifica a nivel de integración.
 * Las reglas de negocio (nombre duplicado, capacidad excedida, etc.)
 * se prueban en RoomServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    // DTOs reutilizables
    private RoomResponseDTO salaMock;
    private RoomRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        salaMock = new RoomResponseDTO();
        salaMock.setIdRoom(1);
        salaMock.setNameRoom("Sala A");
        salaMock.setCapacity(100);
        salaMock.setStatus("Activo");
        salaMock.setIdVenue(1);
        salaMock.setVenueName("CineMax San Isidro");

        requestValido = new RoomRequestDTO();
        requestValido.setNameRoom("Sala A");
        requestValido.setNumRows(10);
        requestValido.setSeatsPerRow(10);
        requestValido.setCapacity(100);
        requestValido.setStatus("Activo");
        requestValido.setIdVenue(1);
    }

    // ── getAllRooms() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllRooms()")
    class GetAllRooms {

        @Test
        @DisplayName("Retorna 200 con la lista completa de salas cuando el Service responde correctamente")
        void retorna200ConListaDeSalas() {
            when(roomService.getAllRooms()).thenReturn(List.of(salaMock));

            ResponseEntity<List<RoomResponseDTO>> response = roomController.getAllRooms();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getNameRoom()).isEqualTo("Sala A");
            verify(roomService, times(1)).getAllRooms();
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si no hay salas registradas")
        void retorna200ConListaVacia() {
            when(roomService.getAllRooms()).thenReturn(List.of());

            ResponseEntity<List<RoomResponseDTO>> response = roomController.getAllRooms();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ── getRoomsByVenue() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRoomsByVenue()")
    class GetRoomsByVenue {

        @Test
        @DisplayName("Retorna 200 con las salas de la sede indicada cuando el Service responde correctamente")
        void retorna200ConSalasDeLaSede() {
            RoomResponseDTO salaB = new RoomResponseDTO();
            salaB.setIdRoom(2);
            salaB.setNameRoom("Sala B");
            salaB.setIdVenue(1);

            when(roomService.getRoomsByVenue(1)).thenReturn(List.of(salaMock, salaB));

            ResponseEntity<List<RoomResponseDTO>> response = roomController.getRoomsByVenue(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody()).extracting(RoomResponseDTO::getIdVenue)
                    .containsOnly(1);
            verify(roomService, times(1)).getRoomsByVenue(1);
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si la sede no tiene salas registradas")
        void retorna200ConListaVaciaSiSedeNoTieneSalas() {
            when(roomService.getRoomsByVenue(99)).thenReturn(List.of());

            ResponseEntity<List<RoomResponseDTO>> response = roomController.getRoomsByVenue(99);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ── createRoom() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createRoom()")
    class CreateRoom {

        @Test
        @DisplayName("Retorna 200 con la sala creada cuando todos los datos son válidos")
        void retorna200ConSalaCreada() {
            when(roomService.createRoom(any(RoomRequestDTO.class))).thenReturn(salaMock);

            ResponseEntity<RoomResponseDTO> response = roomController.createRoom(requestValido);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getNameRoom()).isEqualTo("Sala A");
            assertThat(response.getBody().getCapacity()).isEqualTo(100);
            verify(roomService, times(1)).createRoom(requestValido);
        }

        @Test
        @DisplayName("Propaga la excepción si el Service rechaza la creación por nombre duplicado en la sede")
        void propagaExcepcionSiNombreDuplicadoEnLaSede() {
            when(roomService.createRoom(any(RoomRequestDTO.class)))
                    .thenThrow(new RuntimeException(
                            "Ya existe una sala con el nombre 'Sala A' en este cine."));

            assertThatThrownBy(() -> roomController.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Ya existe una sala con el nombre 'Sala A'");

            verify(roomService, times(1)).createRoom(requestValido);
        }

        @Test
        @DisplayName("Propaga la excepción si la distribución de asientos supera la capacidad total")
        void propagaExcepcionSiDistribucionSuperaCapacidad() {
            // 15 filas x 10 cols = 150, pero capacity = 100
            requestValido.setNumRows(15);
            when(roomService.createRoom(any(RoomRequestDTO.class)))
                    .thenThrow(new RuntimeException(
                            "La distribución (150 butacas) supera la capacidad total permitida (100)."));

            assertThatThrownBy(() -> roomController.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("supera la capacidad total permitida");
        }

        @Test
        @DisplayName("Propaga la excepción si la sede seleccionada no existe")
        void propagaExcepcionSiSedeNoExiste() {
            requestValido.setIdVenue(999);
            when(roomService.createRoom(any(RoomRequestDTO.class)))
                    .thenThrow(new RuntimeException("La sede seleccionada no existe"));

            assertThatThrownBy(() -> roomController.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sede seleccionada no existe");
        }
    }

    // ── updateRoom() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateRoom()")
    class UpdateRoom {

        @Test
        @DisplayName("Retorna 200 con la sala actualizada cuando el id existe y los datos son válidos")
        void retorna200ConSalaActualizada() {
            RoomResponseDTO salaActualizada = new RoomResponseDTO();
            salaActualizada.setIdRoom(1);
            salaActualizada.setNameRoom("Sala A Premium");
            salaActualizada.setCapacity(100);
            salaActualizada.setStatus("Activo");
            salaActualizada.setIdVenue(1);

            requestValido.setNameRoom("Sala A Premium");
            when(roomService.updateRoom(eq(1), any(RoomRequestDTO.class)))
                    .thenReturn(salaActualizada);

            ResponseEntity<RoomResponseDTO> response =
                    roomController.updateRoom(1, requestValido);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getNameRoom()).isEqualTo("Sala A Premium");
            verify(roomService, times(1)).updateRoom(1, requestValido);
        }

        @Test
        @DisplayName("Propaga la excepción si el Service no encuentra la sala a actualizar")
        void propagaExcepcionSiSalaNoExiste() {
            when(roomService.updateRoom(eq(99), any(RoomRequestDTO.class)))
                    .thenThrow(new RuntimeException("La sala no existe"));

            assertThatThrownBy(() -> roomController.updateRoom(99, requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sala no existe");
        }

        @Test
        @DisplayName("Propaga la excepción si la sede de destino en la actualización no existe")
        void propagaExcepcionSiSedeDestinoNoExiste() {
            requestValido.setIdVenue(999);
            when(roomService.updateRoom(eq(1), any(RoomRequestDTO.class)))
                    .thenThrow(new RuntimeException("La sede seleccionada no existe"));

            assertThatThrownBy(() -> roomController.updateRoom(1, requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sede seleccionada no existe");
        }
    }
}