package com.cinemax.backend.service.showtime;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.model.entity.Movie;
import com.cinemax.backend.model.entity.Room;
import com.cinemax.backend.model.entity.Showtime;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.repository.MovieRepository;
import com.cinemax.backend.repository.RoomRepository;
import com.cinemax.backend.repository.ShowtimeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de ShowtimeServiceImpl (HU-14 - Gestión de Funciones/Showtimes).
 *
 * Cubre todos los métodos de la interfaz ShowtimeService verificando:
 *   - mapeo correcto de entidades Showtime a DTOs
 *   - todas las validaciones de negocio previas al guardado (película inactiva, sala
 *     inactiva, fecha pasada, conflicto de horario, permisos de sede)
 *   - cálculo correcto de endTime (startTime + durationMinutes + 30 min de limpieza)
 *   - lógica de cancelación (estados no cancelables)
 *   - que showtimeRepository.save() solo se invoca cuando todas las validaciones pasan
 */
@ExtendWith(MockitoExtension.class)
class ShowtimeServiceImplTest {

    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ShowtimeServiceImpl showtimeService;

    // Entidades reutilizables
    private Venue sedeSanIsidro;
    private Movie peliculaActiva;
    private Movie peliculaInactiva;
    private Room salaActiva;
    private Room salaInactiva;
    private Showtime funcionProgramada;
    private ShowtimeRequestDTO requestValido;

    // Fecha futura fija para que los tests no dependan de la fecha del sistema
    private final LocalDate FECHA_FUTURA = LocalDate.now().plusDays(5);
    private final LocalDate FECHA_PASADA = LocalDate.now().minusDays(1);

    @BeforeEach
    void setUp() {
        sedeSanIsidro = Venue.builder()
                .idVenue(1)
                .nameVenue("CineMax San Isidro")
                .build();

        peliculaActiva = Movie.builder()
                .idMovie(1)
                .titleMovie("Inception")
                .durationMinutes(148)
                .isActive(true)
                .posterUrl("https://cloudinary.com/inception.jpg")
                .build();

        peliculaInactiva = Movie.builder()
                .idMovie(2)
                .titleMovie("Película Retirada")
                .durationMinutes(120)
                .isActive(false)
                .build();

        salaActiva = Room.builder()
                .idRoom(1)
                .nameRoom("Sala A")
                .capacity(100)
                .status("Activo")
                .venue(sedeSanIsidro)
                .build();

        salaInactiva = Room.builder()
                .idRoom(2)
                .nameRoom("Sala B")
                .capacity(80)
                .status("Inactivo")
                .venue(sedeSanIsidro)
                .build();

        funcionProgramada = Showtime.builder()
                .idShowtime(10)
                .showDate(FECHA_FUTURA)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 58))   // 148 min + 30 = 178 min desde 18:00
                .languageFormat("Doblada 2D")
                .baseTicketPrice(new BigDecimal("15.00"))
                .availableSeats(100)
                .status("Programada")
                .movie(peliculaActiva)
                .room(salaActiva)
                .build();

        requestValido = new ShowtimeRequestDTO();
        requestValido.setIdMovie(1);
        requestValido.setIdRoom(1);
        requestValido.setShowDate(FECHA_FUTURA);
        requestValido.setStartTime(LocalTime.of(18, 0));
        requestValido.setLanguageFormat("Doblada 2D");
        requestValido.setBaseTicketPrice(new BigDecimal("15.00"));
    }

    // ── getShowtimesByMovie() ────────────────────────────────────────────────

    @Nested
    @DisplayName("getShowtimesByMovie()")
    class GetShowtimesByMovie {

        @Test
        @DisplayName("Retorna lista de DTOs mapeados cuando hay funciones programadas para la película")
        void retornaListaDeFuncionesProgamadas() {
            when(showtimeRepository.findByMovie_IdMovieAndStatus(1, "Programada"))
                    .thenReturn(List.of(funcionProgramada));

            List<ShowtimeDTO> resultado = showtimeService.getShowtimesByMovie(1);

            assertThat(resultado).hasSize(1);
            ShowtimeDTO dto = resultado.get(0);
            assertThat(dto.getIdShowtime()).isEqualTo(10);
            assertThat(dto.getTitleMovie()).isEqualTo("Inception");
            assertThat(dto.getNameRoom()).isEqualTo("Sala A");
            assertThat(dto.getNameVenue()).isEqualTo("CineMax San Isidro");
            assertThat(dto.getStatus()).isEqualTo("Programada");
        }

        @Test
        @DisplayName("Retorna lista vacía si la película no tiene funciones programadas")
        void retornaListaVaciaSinFunciones() {
            when(showtimeRepository.findByMovie_IdMovieAndStatus(99, "Programada"))
                    .thenReturn(List.of());

            List<ShowtimeDTO> resultado = showtimeService.getShowtimesByMovie(99);

            assertThat(resultado).isEmpty();
        }
    }

    // ── getShowtimeSummary() ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getShowtimeSummary()")
    class GetShowtimeSummary {

        @Test
        @DisplayName("Retorna el DTO resumen con los campos correctos para el flujo de compra")
        void retornaResumenCorrectamente() {
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));

            ShowtimeSummaryDTO resultado = showtimeService.getShowtimeSummary(10);

            assertThat(resultado.getTitleMovie()).isEqualTo("Inception");
            assertThat(resultado.getPosterUrl()).isEqualTo("https://cloudinary.com/inception.jpg");
            assertThat(resultado.getNameVenue()).isEqualTo("CineMax San Isidro");
            assertThat(resultado.getShowDate()).isEqualTo(FECHA_FUTURA);
            assertThat(resultado.getStartTime()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la función no existe")
        void lanzaExcepcionSiFuncionNoExiste() {
            when(showtimeRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.getShowtimeSummary(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Función no encontrada");
        }
    }

    // ── getTicketFares() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTicketFares()")
    class GetTicketFares {

        @Test
        @DisplayName("Retorna exactamente 4 tarifas calculadas correctamente sobre el precio base")
        void retornaCuatroTarifasCalculadasCorrectamente() {
            // precio base = 15.00 → niño = 4.00, adulto mayor = 6.00, discapacitado = 6.00
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));

            List<TicketFareDTO> tarifas = showtimeService.getTicketFares(10);

            assertThat(tarifas).hasSize(4);
            assertThat(tarifas).extracting(TicketFareDTO::getCategoryCode)
                    .containsExactly("ADULTO", "NINO", "ADULTO_MAYOR", "DISCAPACITADO");

            TicketFareDTO adulto     = tarifas.get(0);
            TicketFareDTO nino       = tarifas.get(1);
            TicketFareDTO adultoMayor = tarifas.get(2);

            assertThat(adulto.getPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
            // niño: base − 11
            assertThat(nino.getPrice()).isEqualByComparingTo(new BigDecimal("4.00"));
            // adulto mayor: base − 9
            assertThat(adultoMayor.getPrice()).isEqualByComparingTo(new BigDecimal("6.00"));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la función no existe")
        void lanzaExcepcionSiFuncionNoExiste() {
            when(showtimeRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.getTicketFares(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Función no encontrada");
        }
    }

    // ── createShowtime() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("createShowtime()")
    class CreateShowtime {

        @Test
        @DisplayName("CP-SHW-01: crea la función con endTime correcto (startTime + duración + 30 min)")
        void creaFuncionConEndTimeCalculadoCorrectamente() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));
            // Sin conflictos de horario
            when(showtimeRepository.countConflictingShowtime(any(), any(), any(), any()))
                    .thenReturn(0L);
            when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> {
                Showtime s = inv.getArgument(0);
                s.setIdShowtime(10);
                return s;
            });

            ShowtimeDTO resultado = showtimeService.createShowtime(requestValido, null);

            assertThat(resultado).isNotNull();
            // endTime = 18:00 + 148 min + 30 min = 18:00 + 178 min = 20:58
            assertThat(resultado.getEndTime()).isEqualTo(LocalTime.of(20, 58));
            assertThat(resultado.getStatus()).isEqualTo("Programada");
            verify(showtimeRepository, times(1)).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-02: lanza RuntimeException si la película seleccionada no existe")
        void lanzaExcepcionSiPeliculaNoExiste() {
            when(movieRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La película seleccionada no existe.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-03: lanza RuntimeException si la película está inactiva (inhabilitada)")
        void lanzaExcepcionSiPeliculaEstaInactiva() {
            requestValido.setIdMovie(2);
            when(movieRepository.findById(2)).thenReturn(Optional.of(peliculaInactiva));

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se puede programar una película inactiva.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-04: lanza RuntimeException si la sala seleccionada no existe")
        void lanzaExcepcionSiSalaNoExiste() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La sala seleccionada no existe.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-05: lanza RuntimeException si la sala está inactiva")
        void lanzaExcepcionSiSalaEstaInactiva() {
            requestValido.setIdRoom(2);
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(2)).thenReturn(Optional.of(salaInactiva));

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("está inactiva y no puede recibir funciones.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-06: lanza RuntimeException si la fecha de la función es pasada")
        void lanzaExcepcionSiFechaEsPasada() {
            requestValido.setShowDate(FECHA_PASADA);
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se puede programar una función en una fecha pasada.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-07: lanza RuntimeException si la sala ya tiene una función que se cruza con el horario")
        void lanzaExcepcionSiHayConflictoDeHorario() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));
            // Simula que ya existe una función que se superpone
            when(showtimeRepository.countConflictingShowtime(any(), any(), any(), any()))
                    .thenReturn(1L);

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ya tiene una función que cruza con el horario");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-08: lanza RuntimeException si el gerente de sede intenta programar en otra sede")
        void lanzaExcepcionSiGerenteIntentaProgramarEnOtraSede() {
            // La sala pertenece a sede 1, pero el callerVenueId es 2 (otra sede)
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));

            assertThatThrownBy(() -> showtimeService.createShowtime(requestValido, 2))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No tienes permiso para programar funciones en otra sede.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }
    }

    // ── updateShowtime() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateShowtime()")
    class UpdateShowtime {

        @Test
        @DisplayName("CP-SHW-09: actualiza la función cuando está en estado Programada y los datos son válidos")
        void actualizaFuncionProgramadaCorrectamente() {
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));
            when(showtimeRepository.countConflictingShowtimeExcluding(any(), any(), any(), any(), any()))
                    .thenReturn(0L);
            when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

            requestValido.setLanguageFormat("Subtitulada 3D");
            ShowtimeDTO resultado = showtimeService.updateShowtime(10, requestValido, null);

            assertThat(resultado.getLanguageFormat()).isEqualTo("Subtitulada 3D");
            verify(showtimeRepository, times(1)).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la función a editar no existe")
        void lanzaExcepcionSiFuncionNoExiste() {
            when(showtimeRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.updateShowtime(99, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La función no existe.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-10: lanza RuntimeException si la función no está en estado Programada")
        void lanzaExcepcionSiFuncionNoEstaProgramada() {
            Showtime funcionFinalizada = Showtime.builder()
                    .idShowtime(10)
                    .showDate(FECHA_FUTURA)
                    .status("Finalizada")
                    .movie(peliculaActiva)
                    .room(salaActiva)
                    .build();
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionFinalizada));

            assertThatThrownBy(() -> showtimeService.updateShowtime(10, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Solo se pueden editar funciones en estado 'Programada'.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-11: lanza RuntimeException si se intenta editar una función de fecha pasada")
        void lanzaExcepcionSiFuncionEsDeFechaPasada() {
            Showtime funcionPasada = Showtime.builder()
                    .idShowtime(10)
                    .showDate(FECHA_PASADA)
                    .status("Programada")
                    .movie(peliculaActiva)
                    .room(salaActiva)
                    .build();
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionPasada));

            assertThatThrownBy(() -> showtimeService.updateShowtime(10, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se puede modificar una función de una fecha pasada.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-12: lanza RuntimeException si hay conflicto de horario al actualizar")
        void lanzaExcepcionSiHayConflictoAlActualizar() {
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(1)).thenReturn(Optional.of(salaActiva));
            when(showtimeRepository.countConflictingShowtimeExcluding(any(), any(), any(), any(), any()))
                    .thenReturn(1L);

            assertThatThrownBy(() -> showtimeService.updateShowtime(10, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ya tiene una función que cruza con el horario");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si el gerente intenta mover la función a una sala de otra sede")
        void lanzaExcepcionSiGerenteIntentaMoverFuncionAOtraSede() {
            Venue sedeMiraflores = Venue.builder().idVenue(2).nameVenue("CineMax Miraflores").build();
            Room salaDeMiraflores = Room.builder()
                    .idRoom(3).nameRoom("Sala C").capacity(90)
                    .status("Activo").venue(sedeMiraflores).build();

            requestValido.setIdRoom(3);
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaActiva));
            when(roomRepository.findById(3)).thenReturn(Optional.of(salaDeMiraflores));

            // callerVenueId=1 (San Isidro) intenta mover a sala de sede 2 (Miraflores)
            assertThatThrownBy(() -> showtimeService.updateShowtime(10, requestValido, 1))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No puedes mover una función a una sala de otra sede.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }
    }

    // ── cancelShowtime() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancelShowtime()")
    class CancelShowtime {

        @Test
        @DisplayName("CP-SHW-13: cancela la función correctamente cambiando su estado a Cancelada")
        void cancelaLaFuncionCorrectamente() {
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));
            when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

            showtimeService.cancelShowtime(10, null);

            assertThat(funcionProgramada.getStatus()).isEqualTo("Cancelada");
            verify(showtimeRepository, times(1)).save(funcionProgramada);
        }

        @Test
        @DisplayName("Lanza RuntimeException si la función a cancelar no existe")
        void lanzaExcepcionSiFuncionNoExiste() {
            when(showtimeRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> showtimeService.cancelShowtime(99, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La función no existe.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-14: lanza RuntimeException si la función ya está cancelada")
        void lanzaExcepcionSiYaEstaCancelada() {
            Showtime funcionCancelada = Showtime.builder()
                    .idShowtime(10).status("Cancelada")
                    .movie(peliculaActiva).room(salaActiva).build();
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionCancelada));

            assertThatThrownBy(() -> showtimeService.cancelShowtime(10, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Esta función ya está cancelada.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("CP-SHW-15: lanza RuntimeException si la función ya finalizó")
        void lanzaExcepcionSiYaFinalizo() {
            Showtime funcionFinalizada = Showtime.builder()
                    .idShowtime(10).status("Finalizada")
                    .movie(peliculaActiva).room(salaActiva).build();
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionFinalizada));

            assertThatThrownBy(() -> showtimeService.cancelShowtime(10, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se puede cancelar una función ya finalizada.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si el gerente de sede intenta cancelar función de otra sede")
        void lanzaExcepcionSiGerenteIntentaCancelarFuncionDeOtraSede() {
            // La función pertenece a sede 1, el caller es de sede 2
            when(showtimeRepository.findById(10)).thenReturn(Optional.of(funcionProgramada));

            assertThatThrownBy(() -> showtimeService.cancelShowtime(10, 2))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No tienes permiso para cancelar funciones de otra sede.");

            verify(showtimeRepository, never()).save(any(Showtime.class));
        }
    }
}