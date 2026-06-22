package com.cinemax.backend.service.room;

import com.cinemax.backend.model.dto.room.RoomRequestDTO;
import com.cinemax.backend.model.dto.room.RoomResponseDTO;
import com.cinemax.backend.model.entity.Room;
import com.cinemax.backend.model.entity.Seat;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.RoomRepository;
import com.cinemax.backend.repository.SeatRepository;
import com.cinemax.backend.repository.VenueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de RoomServiceImpl (HU-16 - Gestión de Salas).
 *
 * Cubre todos los métodos de la interfaz RoomService verificando:
 *   - mapeo correcto de entidades Room a RoomResponseDTO
 *   - validaciones de negocio previas al guardado (nombre duplicado, capacidad excedida)
 *   - generación automática de asientos (filas x columnas) en createRoom()
 *   - que roomRepository.save() y seatRepository.saveAll() solo se invocan en casos válidos
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    // Entidades reutilizables
    private Venue sedeSanIsidro;
    private Room salaMock;
    private RoomRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        sedeSanIsidro = Venue.builder()
                .idVenue(1)
                .nameVenue("CineMax San Isidro")
                .build();

        salaMock = Room.builder()
                .idRoom(1)
                .nameRoom("Sala A")
                .capacity(100)
                .numRows(10)
                .seatsPerRow(10)
                .status("Activo")
                .venue(sedeSanIsidro)
                .build();

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
        @DisplayName("Retorna la lista de DTOs mapeados cuando hay salas registradas")
        void retornaListaDeSalasMapeadas() {
            when(roomRepository.findAll()).thenReturn(List.of(salaMock));

            List<RoomResponseDTO> resultado = roomService.getAllRooms();

            assertThat(resultado).hasSize(1);
            RoomResponseDTO dto = resultado.get(0);
            assertThat(dto.getIdRoom()).isEqualTo(1);
            assertThat(dto.getNameRoom()).isEqualTo("Sala A");
            assertThat(dto.getCapacity()).isEqualTo(100);
            assertThat(dto.getStatus()).isEqualTo("Activo");
            assertThat(dto.getIdVenue()).isEqualTo(1);
            assertThat(dto.getVenueName()).isEqualTo("CineMax San Isidro");
        }

        @Test
        @DisplayName("Retorna lista vacía si no hay salas registradas en el sistema")
        void retornaListaVaciaSiNoHaySalas() {
            when(roomRepository.findAll()).thenReturn(List.of());

            List<RoomResponseDTO> resultado = roomService.getAllRooms();

            assertThat(resultado).isEmpty();
        }
    }

    // ── getRoomsByVenue() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRoomsByVenue()")
    class GetRoomsByVenue {

        @Test
        @DisplayName("Retorna solo las salas que pertenecen a la sede indicada")
        void retornaSolasSalasDeLaSede() {
            Venue sedeOtra = Venue.builder().idVenue(2).nameVenue("CineMax Miraflores").build();
            Room salaOtraSede = Room.builder()
                    .idRoom(2).nameRoom("Sala X").capacity(80).status("Activo").venue(sedeOtra).build();

            when(roomRepository.findAll()).thenReturn(List.of(salaMock, salaOtraSede));

            List<RoomResponseDTO> resultado = roomService.getRoomsByVenue(1);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNameRoom()).isEqualTo("Sala A");
            assertThat(resultado.get(0).getIdVenue()).isEqualTo(1);
        }

        @Test
        @DisplayName("Retorna lista vacía si la sede no tiene salas asignadas")
        void retornaListaVaciaSiSedeNoTieneSalas() {
            when(roomRepository.findAll()).thenReturn(List.of(salaMock));

            // Se pide la sede 99 que no tiene ninguna sala
            List<RoomResponseDTO> resultado = roomService.getRoomsByVenue(99);

            assertThat(resultado).isEmpty();
        }
    }

    // ── createRoom() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createRoom()")
    class CreateRoom {

        @Test
        @DisplayName("CP-ROOM-01: crea la sala y genera asientos automáticamente cuando todos los datos son válidos")
        void creaLaSalaYGeneraAsientosAutomaticamente() {
            when(roomRepository.existsByNameRoomAndVenue_IdVenue("Sala A", 1)).thenReturn(false);
            when(venueRepository.findById(1)).thenReturn(Optional.of(sedeSanIsidro));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
                Room r = inv.getArgument(0);
                r.setIdRoom(1);
                return r;
            });
            when(seatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            RoomResponseDTO resultado = roomService.createRoom(requestValido);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNameRoom()).isEqualTo("Sala A");
            assertThat(resultado.getCapacity()).isEqualTo(100);

            // Verifica que se generaron exactamente filas × columnas asientos
            ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
            verify(seatRepository, times(1)).saveAll(captor.capture());
            List<Seat> asientosGenerados = captor.getValue();
            assertThat(asientosGenerados).hasSize(100); // 10 filas × 10 cols
        }

        @Test
        @DisplayName("CP-ROOM-02: los asientos se generan con la nomenclatura correcta (A1, A2... J10)")
        void generaAsientosConNomenclaturaCorrecta() {
            when(roomRepository.existsByNameRoomAndVenue_IdVenue("Sala A", 1)).thenReturn(false);
            when(venueRepository.findById(1)).thenReturn(Optional.of(sedeSanIsidro));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
                Room r = inv.getArgument(0);
                r.setIdRoom(1);
                return r;
            });
            when(seatRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            roomService.createRoom(requestValido);

            ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
            verify(seatRepository).saveAll(captor.capture());
            List<Seat> asientos = captor.getValue();

            // Primer asiento debe ser fila A, columna 1
            assertThat(asientos.get(0).getRowName()).isEqualTo("A");
            assertThat(asientos.get(0).getColumnNumber()).isEqualTo(1);

            // Último asiento debe ser fila J (décima letra), columna 10
            Seat ultimo = asientos.get(asientos.size() - 1);
            assertThat(ultimo.getRowName()).isEqualTo("J");
            assertThat(ultimo.getColumnNumber()).isEqualTo(10);

            // Todos los asientos nacen con estado ACTIVO
            assertThat(asientos).allMatch(s -> "ACTIVO".equals(s.getStatus()));
        }

        @Test
        @DisplayName("CP-ROOM-03: lanza RuntimeException si ya existe una sala con el mismo nombre en la sede")
        void lanzaExcepcionSiNombreDuplicadoEnLaSede() {
            when(roomRepository.existsByNameRoomAndVenue_IdVenue("Sala A", 1)).thenReturn(true);

            assertThatThrownBy(() -> roomService.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Ya existe una sala con el nombre 'Sala A' en este cine.");

            verify(roomRepository, never()).save(any(Room.class));
            verify(seatRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("CP-ROOM-04: lanza RuntimeException si la distribución (filas × cols) supera la capacidad")
        void lanzaExcepcionSiDistribucionSuperaCapacidad() {
            // 15 filas × 10 cols = 150 butacas > 100 de capacidad
            requestValido.setNumRows(15);
            when(roomRepository.existsByNameRoomAndVenue_IdVenue("Sala A", 1)).thenReturn(false);

            assertThatThrownBy(() -> roomService.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("supera la capacidad total permitida");

            verify(roomRepository, never()).save(any(Room.class));
            verify(seatRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("CP-ROOM-05: lanza RuntimeException si la sede asignada no existe en la BD")
        void lanzaExcepcionSiSedeNoExiste() {
            requestValido.setIdVenue(999);
            when(roomRepository.existsByNameRoomAndVenue_IdVenue("Sala A", 999)).thenReturn(false);
            when(venueRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.createRoom(requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sede seleccionada no existe");

            verify(roomRepository, never()).save(any(Room.class));
            verify(seatRepository, never()).saveAll(anyList());
        }
    }

    // ── updateRoom() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateRoom()")
    class UpdateRoom {

        @Test
        @DisplayName("CP-ROOM-06: actualiza los campos de la sala cuando el id existe y los datos son válidos")
        void actualizaLaSalaConDatosValidos() {
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaMock));
            when(venueRepository.findById(1)).thenReturn(Optional.of(sedeSanIsidro));
            when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

            requestValido.setNameRoom("Sala A Premium");
            RoomResponseDTO resultado = roomService.updateRoom(1, requestValido);

            assertThat(resultado.getNameRoom()).isEqualTo("Sala A Premium");
            verify(roomRepository, times(1)).save(any(Room.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la sala a actualizar no existe")
        void lanzaExcepcionSiSalaNoExiste() {
            when(roomRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.updateRoom(99, requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sala no existe");

            verify(roomRepository, never()).save(any(Room.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la nueva sede de destino no existe")
        void lanzaExcepcionSiSedeDestinoNoExiste() {
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaMock));
            requestValido.setIdVenue(999);
            when(venueRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomService.updateRoom(1, requestValido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sede seleccionada no existe");

            verify(roomRepository, never()).save(any(Room.class));
        }
    }
}