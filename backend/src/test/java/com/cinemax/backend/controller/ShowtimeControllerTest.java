package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.showtime.ShowtimeDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeRequestDTO;
import com.cinemax.backend.model.dto.showtime.ShowtimeSummaryDTO;
import com.cinemax.backend.model.dto.showtime.TicketFareDTO;
import com.cinemax.backend.model.entity.UserAccount;
import com.cinemax.backend.model.entity.Venue;
import com.cinemax.backend.service.showtime.ShowtimeService;

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
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del ShowtimeController (HU-14 - Gestión de Funciones/Showtimes).
 *
 * Estrategia: llamada directa al método Java del Controller, sin levantar Spring.
 * Verifica que el Controller:
 *   - extrae correctamente el idVenue del principal (UserAccount) en el Authentication
 *   - aplica la lógica de permisos: MANAGE_VENUES (Gerente General) vs gerente de sede
 *   - pasa el callerVenueId correcto al ShowtimeService
 *   - devuelve 200 en éxito y 400 (badRequest) cuando el Service lanza RuntimeException
 *   - retorna 403 cuando un gerente de sede intenta ver funciones de otra sede
 *
 * Nota: @PreAuthorize se verifica a nivel de integración.
 * Las reglas de negocio (conflicto de horario, sala inactiva, etc.)
 * se prueban en ShowtimeServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
class ShowtimeControllerTest {

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private ShowtimeController showtimeController;

    // Objetos de autenticación reutilizables
    private Authentication authGerGeneral;      // tiene MANAGE_VENUES, sin sede
    private Authentication authGerenteSede;     // sin MANAGE_VENUES, sede id=1

    // DTOs reutilizables
    private ShowtimeDTO funcionMock;
    private ShowtimeRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        // ── Gerente General: tiene MANAGE_VENUES, no pertenece a ninguna sede ──
        Venue sinSede = null;
        UserAccount gerGeneral = UserAccount.builder()
                .idUser(1).email("gerente.general@cinemax.com").venue(sinSede).build();

        authGerGeneral = mock(Authentication.class);
        when(authGerGeneral.getPrincipal()).thenReturn(gerGeneral);
        when(authGerGeneral.getAuthorities()).thenAnswer(inv ->
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("MANAGE_VENUES"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("MANAGE_SHOWTIMES")));

        // ── Gerente de Sede: NO tiene MANAGE_VENUES, pertenece a sede id=1 ──
        Venue sedeSanIsidro = Venue.builder().idVenue(1).nameVenue("CineMax San Isidro").build();
        UserAccount gerenteSede = UserAccount.builder()
                .idUser(2).email("gerente.sede@cinemax.com").venue(sedeSanIsidro).build();

        authGerenteSede = mock(Authentication.class);
        when(authGerenteSede.getPrincipal()).thenReturn(gerenteSede);
        when(authGerenteSede.getAuthorities()).thenAnswer(inv ->
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("MANAGE_SHOWTIMES")));

        // ── DTO de función reutilizable ──
        funcionMock = new ShowtimeDTO();
        funcionMock.setIdShowtime(10);
        funcionMock.setIdMovie(1);
        funcionMock.setTitleMovie("Inception");
        funcionMock.setIdRoom(1);
        funcionMock.setNameRoom("Sala A");
        funcionMock.setIdVenue(1);
        funcionMock.setNameVenue("CineMax San Isidro");
        funcionMock.setShowDate(LocalDate.now().plusDays(1));
        funcionMock.setStartTime(LocalTime.of(18, 0));
        funcionMock.setLanguageFormat("Doblada 2D");
        funcionMock.setStatus("Programada");
        funcionMock.setBaseTicketPrice(new BigDecimal("15.00"));

        // ── Request de creación/actualización reutilizable ──
        requestValido = new ShowtimeRequestDTO();
        requestValido.setIdMovie(1);
        requestValido.setIdRoom(1);
        requestValido.setShowDate(LocalDate.now().plusDays(1));
        requestValido.setStartTime(LocalTime.of(18, 0));
        requestValido.setLanguageFormat("Doblada 2D");
        requestValido.setBaseTicketPrice(new BigDecimal("15.00"));
    }

    // ── getShowtimesByMovie() ────────────────────────────────────────────────

    @Nested
    @DisplayName("getShowtimesByMovie()")
    class GetShowtimesByMovie {

        @Test
        @DisplayName("Retorna 200 con las funciones de la película indicada (endpoint público)")
        void retorna200ConFuncionesDeLaPelicula() {
            when(showtimeService.getShowtimesByMovie(1)).thenReturn(List.of(funcionMock));

            ResponseEntity<List<ShowtimeDTO>> response =
                    showtimeController.getShowtimesByMovie(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitleMovie()).isEqualTo("Inception");
            verify(showtimeService, times(1)).getShowtimesByMovie(1);
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si la película no tiene funciones programadas")
        void retorna200ConListaVacia() {
            when(showtimeService.getShowtimesByMovie(99)).thenReturn(List.of());

            ResponseEntity<List<ShowtimeDTO>> response =
                    showtimeController.getShowtimesByMovie(99);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ── getShowtimesByVenue() ────────────────────────────────────────────────

    @Nested
    @DisplayName("getShowtimesByVenue()")
    class GetShowtimesByVenue {

        @Test
        @DisplayName("Gerente General puede ver funciones de cualquier sede")
        void gerGenPuedeVerFuncionesDeOtraSede() {
            LocalDate fecha = LocalDate.now().plusDays(1);
            when(showtimeService.getShowtimesByVenueAndDate(2, fecha))
                    .thenReturn(List.of(funcionMock));

            ResponseEntity<?> response =
                    showtimeController.getShowtimesByVenue(2, fecha, authGerGeneral);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(showtimeService, times(1)).getShowtimesByVenueAndDate(2, fecha);
        }

        @Test
        @DisplayName("Gerente de sede puede ver funciones de su propia sede")
        void gerenteSedePuedeVerSusPropiasFunciones() {
            LocalDate fecha = LocalDate.now().plusDays(1);
            when(showtimeService.getShowtimesByVenueAndDate(1, fecha))
                    .thenReturn(List.of(funcionMock));

            ResponseEntity<?> response =
                    showtimeController.getShowtimesByVenue(1, fecha, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(showtimeService, times(1)).getShowtimesByVenueAndDate(1, fecha);
        }

        @Test
        @DisplayName("Retorna 403 si el gerente de sede intenta ver funciones de otra sede")
        void retorna403SiGerenteSedePideOtraSede() {
            LocalDate fecha = LocalDate.now().plusDays(1);

            // idVenue=2 pero el gerente pertenece a sede 1
            ResponseEntity<?> response =
                    showtimeController.getShowtimesByVenue(2, fecha, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().toString())
                    .contains("No tienes permiso para ver funciones de otra sede.");
            verify(showtimeService, never()).getShowtimesByVenueAndDate(anyInt(), any());
        }

        @Test
        @DisplayName("Retorna 400 si el Service lanza RuntimeException")
        void retorna400SiServiceLanzaExcepcion() {
            LocalDate fecha = LocalDate.now().plusDays(1);
            when(showtimeService.getShowtimesByVenueAndDate(1, fecha))
                    .thenThrow(new RuntimeException("Error inesperado al consultar funciones."));

            ResponseEntity<?> response =
                    showtimeController.getShowtimesByVenue(1, fecha, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo("Error inesperado al consultar funciones.");
        }
    }

    // ── getSummary() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSummary()")
    class GetSummary {

        @Test
        @DisplayName("Retorna 200 con el resumen de la función para el flujo de compra (endpoint público)")
        void retorna200ConResumenDeLaFuncion() {
            ShowtimeSummaryDTO summary = new ShowtimeSummaryDTO();
            summary.setTitleMovie("Inception");
            summary.setNameVenue("CineMax San Isidro");
            summary.setShowDate(LocalDate.now().plusDays(1));
            summary.setStartTime(LocalTime.of(18, 0));

            when(showtimeService.getShowtimeSummary(10)).thenReturn(summary);

            ResponseEntity<ShowtimeSummaryDTO> response = showtimeController.getSummary(10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getTitleMovie()).isEqualTo("Inception");
            verify(showtimeService, times(1)).getShowtimeSummary(10);
        }
    }

    // ── getTicketFares() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTicketFares()")
    class GetTicketFares {

        @Test
        @DisplayName("Retorna 200 con las 4 tarifas calculadas para la función indicada")
        void retorna200ConLasCuatroTarifas() {
            List<TicketFareDTO> tarifas = List.of(
                    new TicketFareDTO("ADULTO",        "Adulto",                 new BigDecimal("15.00")),
                    new TicketFareDTO("NINO",          "Niño",                   new BigDecimal("4.00")),
                    new TicketFareDTO("ADULTO_MAYOR",  "Adulto Mayor",           new BigDecimal("6.00")),
                    new TicketFareDTO("DISCAPACITADO", "Personas Discapacitadas", new BigDecimal("6.00"))
            );
            when(showtimeService.getTicketFares(10)).thenReturn(tarifas);

            ResponseEntity<List<TicketFareDTO>> response = showtimeController.getTicketFares(10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(4);
            assertThat(response.getBody()).extracting(TicketFareDTO::getCategoryCode)
                    .containsExactly("ADULTO", "NINO", "ADULTO_MAYOR", "DISCAPACITADO");
            verify(showtimeService, times(1)).getTicketFares(10);
        }
    }

    // ── createShowtime() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("createShowtime()")
    class CreateShowtime {

        @Test
        @DisplayName("Gerente General crea función en cualquier sede (callerVenueId=null)")
        void gerGenCreaFuncionEnCualquierSede() {
            when(showtimeService.createShowtime(any(ShowtimeRequestDTO.class), isNull()))
                    .thenReturn(funcionMock);

            ResponseEntity<?> response =
                    showtimeController.createShowtime(requestValido, authGerGeneral);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Verifica que se pasa null como callerVenueId para el Gerente General
            verify(showtimeService, times(1)).createShowtime(any(ShowtimeRequestDTO.class), isNull());
        }

        @Test
        @DisplayName("Gerente de sede crea función pasando su idVenue como callerVenueId")
        void gerenteSedeCreaFuncionConSuIdVenue() {
            when(showtimeService.createShowtime(any(ShowtimeRequestDTO.class), eq(1)))
                    .thenReturn(funcionMock);

            ResponseEntity<?> response =
                    showtimeController.createShowtime(requestValido, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Verifica que se pasa el idVenue del gerente (1) al Service
            verify(showtimeService, times(1)).createShowtime(any(ShowtimeRequestDTO.class), eq(1));
        }

        @Test
        @DisplayName("Retorna 400 si el Service rechaza la creación (sala inactiva, fecha pasada, conflicto...)")
        void retorna400SiServiceLanzaRuntimeException() {
            when(showtimeService.createShowtime(any(ShowtimeRequestDTO.class), any()))
                    .thenThrow(new RuntimeException("La sala \"Sala A\" está inactiva y no puede recibir funciones."));

            ResponseEntity<?> response =
                    showtimeController.createShowtime(requestValido, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .isEqualTo("La sala \"Sala A\" está inactiva y no puede recibir funciones.");
        }
    }

    // ── updateShowtime() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateShowtime()")
    class UpdateShowtime {

        @Test
        @DisplayName("Gerente General actualiza cualquier función (callerVenueId=null)")
        void gerGenActualizaFuncionEnCualquierSede() {
            when(showtimeService.updateShowtime(eq(10), any(ShowtimeRequestDTO.class), isNull()))
                    .thenReturn(funcionMock);

            ResponseEntity<?> response =
                    showtimeController.updateShowtime(10, requestValido, authGerGeneral);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(showtimeService, times(1))
                    .updateShowtime(eq(10), any(ShowtimeRequestDTO.class), isNull());
        }

        @Test
        @DisplayName("Gerente de sede actualiza función pasando su idVenue como callerVenueId")
        void gerenteSededActualizaFuncionConSuIdVenue() {
            when(showtimeService.updateShowtime(eq(10), any(ShowtimeRequestDTO.class), eq(1)))
                    .thenReturn(funcionMock);

            ResponseEntity<?> response =
                    showtimeController.updateShowtime(10, requestValido, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(showtimeService, times(1))
                    .updateShowtime(eq(10), any(ShowtimeRequestDTO.class), eq(1));
        }

        @Test
        @DisplayName("Retorna 400 si la función no es editable (ya Finalizada o es de otra sede)")
        void retorna400SiFuncionNoEsEditable() {
            when(showtimeService.updateShowtime(eq(10), any(ShowtimeRequestDTO.class), eq(1)))
                    .thenThrow(new RuntimeException("Solo se pueden editar funciones en estado 'Programada'."));

            ResponseEntity<?> response =
                    showtimeController.updateShowtime(10, requestValido, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .isEqualTo("Solo se pueden editar funciones en estado 'Programada'.");
        }
    }

    // ── cancelShowtime() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancelShowtime()")
    class CancelShowtime {

        @Test
        @DisplayName("Gerente General cancela función y retorna 200 con mensaje de éxito")
        void gerGenCancelaFuncion() {
            doNothing().when(showtimeService).cancelShowtime(eq(10), isNull());

            ResponseEntity<?> response =
                    showtimeController.cancelShowtime(10, authGerGeneral);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Función cancelada exitosamente.");
            verify(showtimeService, times(1)).cancelShowtime(eq(10), isNull());
        }

        @Test
        @DisplayName("Gerente de sede cancela función de su propia sede")
        void gerenteSedeCancelaFuncionPropia() {
            doNothing().when(showtimeService).cancelShowtime(eq(10), eq(1));

            ResponseEntity<?> response =
                    showtimeController.cancelShowtime(10, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Función cancelada exitosamente.");
            verify(showtimeService, times(1)).cancelShowtime(eq(10), eq(1));
        }

        @Test
        @DisplayName("Retorna 400 si la función ya estaba cancelada o ya finalizó")
        void retorna400SiFuncionYaCanceladaOFinalizada() {
            doThrow(new RuntimeException("Esta función ya está cancelada."))
                    .when(showtimeService).cancelShowtime(eq(10), any());

            ResponseEntity<?> response =
                    showtimeController.cancelShowtime(10, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isEqualTo("Esta función ya está cancelada.");
        }

        @Test
        @DisplayName("Retorna 400 si el gerente de sede intenta cancelar función de otra sede")
        void retorna400SiGerenteIntentaCancelarFuncionDeOtraSede() {
            doThrow(new RuntimeException("No tienes permiso para cancelar funciones de otra sede."))
                    .when(showtimeService).cancelShowtime(eq(10), eq(1));

            ResponseEntity<?> response =
                    showtimeController.cancelShowtime(10, authGerenteSede);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .isEqualTo("No tienes permiso para cancelar funciones de otra sede.");
        }
    }
}