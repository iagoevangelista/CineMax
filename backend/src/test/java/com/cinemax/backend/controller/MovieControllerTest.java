package com.cinemax.backend.controller;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.dto.movie.MovieRequestDTO;
import com.cinemax.backend.service.cloudinary.CloudinaryService;
import com.cinemax.backend.service.movie.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del MovieController (HU-13 - Gestión de Películas).
 *
 * Estrategia: llamada directa al método Java del Controller, sin levantar Spring.
 * Verifica que el Controller:
 *   - llama al MovieService con los parámetros correctos
 *   - llama al CloudinaryService para subir la imagen antes de delegar al Service
 *   - devuelve el ResponseEntity con el status HTTP esperado
 *
 * Nota: @PreAuthorize se verifica a nivel de integración.
 * Las reglas de negocio (clasificación inexistente, géneros vacíos, etc.)
 * se prueban en MovieServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private MovieController movieController;

    // DTOs
    private MovieDetailDTO detalleMock;
    private MovieListDTO resumenMock;
    private MovieRequestDTO requestValido;
    private MockMultipartFile fileMock;
    private String movieJson;

    @BeforeEach
    void setUp() throws Exception {
        detalleMock = new MovieDetailDTO();
        detalleMock.setIdMovie(1);
        detalleMock.setTitleMovie("Inception");
        detalleMock.setDirector("Christopher Nolan");
        detalleMock.setDurationMinutes(148);
        detalleMock.setClassificationName("PG-13");
        detalleMock.setGenreNames(List.of("Ciencia Ficción", "Acción"));

        resumenMock = new MovieListDTO();
        resumenMock.setIdMovie(1);
        resumenMock.setTitleMovie("Inception");
        resumenMock.setStatus("Cartelera");
        resumenMock.setDurationMinutes(148);

        requestValido = new MovieRequestDTO();
        requestValido.setTitleMovie("Inception");
        requestValido.setDirector("Christopher Nolan");
        requestValido.setDurationMinutes(148);
        requestValido.setStatus("Cartelera");
        requestValido.setIdClassification(2);
        requestValido.setIdGenres(List.of(1, 3));

        // Serializar el DTO a JSON tal como lo envía el frontend (multipart "movie")
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        movieJson = mapper.writeValueAsString(requestValido);

        fileMock = new MockMultipartFile(
                "file", "inception.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );
    }

    // getMoviesByStatus() 

    @Nested
    @DisplayName("getMoviesByStatus()")
    class GetMoviesByStatus {

        @Test
        @DisplayName("Retorna 200 con lista de películas cuando el Service responde correctamente")
        void retorna200ConListaDePeliculas() {
            when(movieService.getMoviesByStatus("Cartelera")).thenReturn(List.of(resumenMock));

            ResponseEntity<List<MovieListDTO>> response =
                    movieController.getMoviesByStatus("Cartelera");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitleMovie()).isEqualTo("Inception");
            verify(movieService, times(1)).getMoviesByStatus("Cartelera");
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si no hay películas para el estado dado")
        void retorna200ConListaVacia() {
            when(movieService.getMoviesByStatus("Próximamente")).thenReturn(List.of());

            ResponseEntity<List<MovieListDTO>> response =
                    movieController.getMoviesByStatus("Próximamente");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // getMovieById() 

    @Nested
    @DisplayName("getMovieById()")
    class GetMovieById {

        @Test
        @DisplayName("Retorna 200 con el detalle de la película cuando el id existe")
        void retorna200ConDetalleDeLaPelicula() {
            when(movieService.getMovieById(1)).thenReturn(detalleMock);

            ResponseEntity<MovieDetailDTO> response = movieController.getMovieById(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitleMovie()).isEqualTo("Inception");
            verify(movieService, times(1)).getMovieById(1);
        }

        @Test
        @DisplayName("Propaga la excepción si el Service no encuentra la película")
        void propagaExcepcionSiPeliculaNoExiste() {
            when(movieService.getMovieById(999))
                    .thenThrow(new RuntimeException("Película no encontrada con ID: 999"));

            assertThatThrownBy(() -> movieController.getMovieById(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Película no encontrada con ID: 999");
        }
    }

    // createMovie()

    @Nested
    @DisplayName("createMovie()")
    class CreateMovie {

        @Test
        @DisplayName("Retorna 201 Created con el detalle de la película cuando la creación es exitosa")
        void retorna201ConPeliculaCreada() throws IOException {
            when(cloudinaryService.uploadImage(fileMock)).thenReturn("https://cloudinary.com/inception.jpg");
            when(movieService.createMovie(any(MovieRequestDTO.class), anyString()))
                    .thenReturn(detalleMock);

            ResponseEntity<?> response = movieController.createMovie(movieJson, fileMock);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isInstanceOf(MovieDetailDTO.class);
            MovieDetailDTO body = (MovieDetailDTO) response.getBody();
            assertThat(body.getTitleMovie()).isEqualTo("Inception");

            // Verifica que primero sube la imagen y luego delega al Service
            verify(cloudinaryService, times(1)).uploadImage(fileMock);
            verify(movieService, times(1)).createMovie(any(MovieRequestDTO.class),
                    eq("https://cloudinary.com/inception.jpg"));
        }

        @Test
        @DisplayName("Retorna 500 si el CloudinaryService falla al subir la imagen")
        void retorna500SiCloudinaryFalla() throws IOException {
            when(cloudinaryService.uploadImage(fileMock))
                    .thenThrow(new IOException("Error de conexión con Cloudinary"));

            ResponseEntity<?> response = movieController.createMovie(movieJson, fileMock);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().toString())
                    .contains("Error al procesar los datos o subir el póster");
            // Si Cloudinary falla, el MovieService nunca debe ser invocado
            verify(movieService, never()).createMovie(any(), anyString());
        }

        @Test
        @DisplayName("Propaga la excepción si el Service rechaza la creación (clasificación inexistente)")
        void propagaExcepcionSiClasificacionNoExiste() throws IOException {
            when(cloudinaryService.uploadImage(fileMock)).thenReturn("https://cloudinary.com/img.jpg");
            when(movieService.createMovie(any(MovieRequestDTO.class), anyString()))
                    .thenThrow(new RuntimeException("La clasificación seleccionada no existe."));

            assertThatThrownBy(() -> movieController.createMovie(movieJson, fileMock))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La clasificación seleccionada no existe.");
        }
    }

    // updateMovie()

    @Nested
    @DisplayName("updateMovie()")
    class UpdateMovie {

        @Test
        @DisplayName("Retorna 200 con el detalle actualizado cuando se envía un nuevo archivo de póster")
        void retorna200ConPeliculaActualizadaConNuevoPoster() throws IOException {
            when(cloudinaryService.uploadImage(fileMock)).thenReturn("https://cloudinary.com/nuevo.jpg");
            when(movieService.updateMovie(eq(1), any(MovieRequestDTO.class), anyString()))
                    .thenReturn(detalleMock);

            ResponseEntity<MovieDetailDTO> response =
                    movieController.updateMovie(1, movieJson, fileMock);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getTitleMovie()).isEqualTo("Inception");
            verify(cloudinaryService, times(1)).uploadImage(fileMock);
            verify(movieService, times(1)).updateMovie(eq(1), any(MovieRequestDTO.class),
                    eq("https://cloudinary.com/nuevo.jpg"));
        }

        @Test
        @DisplayName("Retorna 200 sin subir imagen si no se envía archivo (edición sin cambiar póster)")
        void retorna200SinSubirImagenSiNoHayArchivo() throws IOException {
            when(movieService.updateMovie(eq(1), any(MovieRequestDTO.class), isNull()))
                    .thenReturn(detalleMock);

            // file = null simula que el frontend no envió un archivo nuevo
            ResponseEntity<MovieDetailDTO> response =
                    movieController.updateMovie(1, movieJson, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Cloudinary NO debe ser invocado si no hay archivo
            verify(cloudinaryService, never()).uploadImage(any());
            verify(movieService, times(1)).updateMovie(eq(1), any(MovieRequestDTO.class), isNull());
        }

        @Test
        @DisplayName("Retorna 500 si el CloudinaryService falla durante la actualización")
        void retorna500SiCloudinaryFallaEnUpdate() throws IOException {
            when(cloudinaryService.uploadImage(fileMock))
                    .thenThrow(new IOException("Timeout al conectar con Cloudinary"));

            ResponseEntity<MovieDetailDTO> response =
                    movieController.updateMovie(1, movieJson, fileMock);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            verify(movieService, never()).updateMovie(any(), any(), any());
        }
    }

    // deleteMovie()

    @Nested
    @DisplayName("deleteMovie()")
    class DeleteMovie {

        @Test
        @DisplayName("Retorna 204 No Content y llama al Service con el id correcto (soft-delete)")
        void retorna204YLlamaAlService() {
            doNothing().when(movieService).deleteMovie(1);

            ResponseEntity<Void> response = movieController.deleteMovie(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(movieService, times(1)).deleteMovie(1);
        }

        @Test
        @DisplayName("Propaga la excepción si el Service no encuentra la película a inhabilitar")
        void propagaExcepcionSiPeliculaNoExiste() {
            doThrow(new RuntimeException("Película no encontrada con ID: 99"))
                    .when(movieService).deleteMovie(99);

            assertThatThrownBy(() -> movieController.deleteMovie(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Película no encontrada con ID: 99");
        }
    }
}