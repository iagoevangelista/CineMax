package com.cinemax.backend.service.movie;

import com.cinemax.backend.model.dto.movie.MovieDetailDTO;
import com.cinemax.backend.model.dto.movie.MovieListDTO;
import com.cinemax.backend.model.dto.movie.MovieRequestDTO;
import com.cinemax.backend.model.entity.Classification;
import com.cinemax.backend.model.entity.Genre;
import com.cinemax.backend.model.entity.Movie;
import com.cinemax.backend.repository.ClassificationRepository;
import com.cinemax.backend.repository.GenreRepository;
import com.cinemax.backend.repository.MovieRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de MovieServiceImpl (HU-13 - Gestión de Películas).
 *
 * Cubre todos los métodos de la interfaz MovieService verificando:
 *   - lógica de mapeo entity → DTO en los métodos de consulta
 *   - reglas de negocio que disparan RuntimeException antes de tocar la BD
 *   - que movieRepository.save() solo se invoca cuando todos los datos son válidos
 *   - que deleteMovie() realiza soft-delete (isActive=false), sin borrar el registro
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ClassificationRepository classificationRepository;
    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    // Entidades reutilizables
    private Classification clasificacionPG13;
    private Genre generoAccion;
    private Genre generoCienciaFiccion;
    private Movie peliculaMock;
    private MovieRequestDTO requestValido;

    @BeforeEach
    void setUp() {
        clasificacionPG13 = Classification.builder()
                .idClassification(2)
                .nameClassification("PG-13")
                .build();

        generoAccion = Genre.builder()
                .idGenre(1)
                .nameGenre("Acción")
                .build();

        generoCienciaFiccion = Genre.builder()
                .idGenre(3)
                .nameGenre("Ciencia Ficción")
                .build();

        peliculaMock = Movie.builder()
                .idMovie(1)
                .titleMovie("Inception")
                .director("Christopher Nolan")
                .synopsis("Un ladrón que roba secretos corporativos mediante sueños.")
                .durationMinutes(148)
                .posterUrl("https://cloudinary.com/inception.jpg")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .status("Cartelera")
                .premiereWeek(false)
                .isActive(true)
                .classification(clasificacionPG13)
                .genres(List.of(generoAccion, generoCienciaFiccion))
                .build();

        requestValido = new MovieRequestDTO();
        requestValido.setTitleMovie("Inception");
        requestValido.setDirector("Christopher Nolan");
        requestValido.setSynopsis("Un ladrón que roba secretos corporativos mediante sueños.");
        requestValido.setDurationMinutes(148);
        requestValido.setReleaseDate(LocalDate.of(2010, 7, 16));
        requestValido.setStatus("Cartelera");
        requestValido.setPremiereWeek(false);
        requestValido.setIdClassification(2);
        requestValido.setIdGenres(List.of(1, 3));
    }

    // ── getMoviesByStatus() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getMoviesByStatus()")
    class GetMoviesByStatus {

        @Test
        @DisplayName("Retorna la lista de DTOs ligeros cuando hay películas activas con el estado dado")
        void retornaListaDePeliculasActivas() {
            when(movieRepository.findByStatusAndIsActiveTrue("Cartelera"))
                    .thenReturn(List.of(peliculaMock));

            List<MovieListDTO> resultado = movieService.getMoviesByStatus("Cartelera");

            assertThat(resultado).hasSize(1);
            MovieListDTO dto = resultado.get(0);
            assertThat(dto.getIdMovie()).isEqualTo(1);
            assertThat(dto.getTitleMovie()).isEqualTo("Inception");
            assertThat(dto.getStatus()).isEqualTo("Cartelera");
            assertThat(dto.getDurationMinutes()).isEqualTo(148);
            // Verifica que el rating se extrae de la clasificación asociada
            assertThat(dto.getRating()).isEqualTo("PG-13");
        }

        @Test
        @DisplayName("Retorna lista vacía si no hay películas activas para el estado solicitado")
        void retornaListaVaciaSiNoHayPeliculas() {
            when(movieRepository.findByStatusAndIsActiveTrue("Próximamente"))
                    .thenReturn(List.of());

            List<MovieListDTO> resultado = movieService.getMoviesByStatus("Próximamente");

            assertThat(resultado).isEmpty();
            verify(movieRepository, times(1)).findByStatusAndIsActiveTrue("Próximamente");
        }
    }

    // ── getMovieById() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMovieById()")
    class GetMovieById {

        @Test
        @DisplayName("Retorna el DTO de detalle correctamente mapeado cuando la película existe")
        void retornaDetalleCuandoPeliculaExiste() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));

            MovieDetailDTO dto = movieService.getMovieById(1);

            assertThat(dto.getIdMovie()).isEqualTo(1);
            assertThat(dto.getTitleMovie()).isEqualTo("Inception");
            assertThat(dto.getDirector()).isEqualTo("Christopher Nolan");
            assertThat(dto.getClassificationName()).isEqualTo("PG-13");
            // Verifica que los nombres de género se mapean desde la lista de entidades
            assertThat(dto.getGenreNames()).containsExactlyInAnyOrder("Acción", "Ciencia Ficción");
        }

        @Test
        @DisplayName("Lanza RuntimeException con mensaje claro si la película no existe")
        void lanzaExcepcionSiPeliculaNoExiste() {
            when(movieRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.getMovieById(999))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Película no encontrada con ID: 999");
        }
    }

    // ── createMovie() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createMovie()")
    class CreateMovie {

        @Test
        @DisplayName("CP-MOV-01: crea la película y retorna el DTO cuando todos los datos son válidos")
        void creaLaPeliculaConDatosValidos() {
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(List.of(1, 3)))
                    .thenReturn(List.of(generoAccion, generoCienciaFiccion));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
                Movie guardada = inv.getArgument(0);
                guardada.setIdMovie(1);
                return guardada;
            });

            MovieDetailDTO resultado = movieService.createMovie(
                    requestValido, "https://cloudinary.com/inception.jpg");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTitleMovie()).isEqualTo("Inception");
            assertThat(resultado.getPosterUrl()).isEqualTo("https://cloudinary.com/inception.jpg");
            assertThat(resultado.getClassificationName()).isEqualTo("PG-13");
            assertThat(resultado.getGenreNames()).containsExactlyInAnyOrder("Acción", "Ciencia Ficción");
            verify(movieRepository, times(1)).save(any(Movie.class));
        }

        @Test
        @DisplayName("CP-MOV-02: lanza RuntimeException si la clasificación no existe")
        void lanzaExcepcionSiClasificacionNoExiste() {
            when(classificationRepository.findById(99)).thenReturn(Optional.empty());
            requestValido.setIdClassification(99);

            assertThatThrownBy(() -> movieService.createMovie(requestValido, "https://img.jpg"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La clasificación seleccionada no existe.");

            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("CP-MOV-03: lanza RuntimeException si la lista de géneros está vacía o son inválidos")
        void lanzaExcepcionSiGenerosSonInvalidos() {
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            // findAllById devuelve vacío cuando ningún id de género existe en la BD
            when(genreRepository.findAllById(any())).thenReturn(List.of());

            assertThatThrownBy(() -> movieService.createMovie(requestValido, "https://img.jpg"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Debe asociar al menos un género válido a la película.");

            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Asigna isActive=true automáticamente al crear una nueva película")
        void asignaIsActiveTrueAlCrear() {
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(any()))
                    .thenReturn(List.of(generoAccion, generoCienciaFiccion));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

            movieService.createMovie(requestValido, "https://img.jpg");

            verify(movieRepository).save(argThat(m -> Boolean.TRUE.equals(m.getIsActive())));
        }
    }

    // ── updateMovie() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMovie()")
    class UpdateMovie {

        @Test
        @DisplayName("CP-MOV-04: actualiza los campos de la película cuando todos los datos son válidos")
        void actualizaLaPeliculaConDatosValidos() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(any()))
                    .thenReturn(List.of(generoAccion, generoCienciaFiccion));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

            requestValido.setTitleMovie("Inception Remastered");
            MovieDetailDTO resultado = movieService.updateMovie(1, requestValido, null);

            assertThat(resultado.getTitleMovie()).isEqualTo("Inception Remastered");
            verify(movieRepository, times(1)).save(any(Movie.class));
        }

        @Test
        @DisplayName("Actualiza el posterUrl solo cuando se proporciona una nueva URL de imagen")
        void actualizaPosterUrlSoloCuandoHayNuevaUrl() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(any()))
                    .thenReturn(List.of(generoAccion, generoCienciaFiccion));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

            movieService.updateMovie(1, requestValido, "https://cloudinary.com/nuevo.jpg");

            verify(movieRepository).save(argThat(m ->
                    "https://cloudinary.com/nuevo.jpg".equals(m.getPosterUrl())));
        }

        @Test
        @DisplayName("Conserva el posterUrl original cuando imageUrl es null (sin cambio de póster)")
        void conservaPosterUrlOriginalCuandoImageUrlEsNull() {
            String urlOriginal = peliculaMock.getPosterUrl();
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(any()))
                    .thenReturn(List.of(generoAccion, generoCienciaFiccion));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

            movieService.updateMovie(1, requestValido, null);

            verify(movieRepository).save(argThat(m -> urlOriginal.equals(m.getPosterUrl())));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la película a actualizar no existe")
        void lanzaExcepcionSiPeliculaNoExiste() {
            when(movieRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.updateMovie(99, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Película no encontrada con ID: 99");

            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si la nueva clasificación no existe")
        void lanzaExcepcionSiNuevaClasificacionNoExiste() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(classificationRepository.findById(2)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.updateMovie(1, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("La clasificación seleccionada no existe.");

            verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Lanza RuntimeException si los nuevos géneros son todos inválidos")
        void lanzaExcepcionSiNuevosGenerosSonInvalidos() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(classificationRepository.findById(2)).thenReturn(Optional.of(clasificacionPG13));
            when(genreRepository.findAllById(any())).thenReturn(List.of());

            assertThatThrownBy(() -> movieService.updateMovie(1, requestValido, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Debe asociar al menos un género válido.");

            verify(movieRepository, never()).save(any(Movie.class));
        }
    }

    // ── deleteMovie() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteMovie()")
    class DeleteMovie {

        @Test
        @DisplayName("CP-MOV-05: realiza soft-delete (isActive=false) sin eliminar el registro de la BD")
        void realizaSoftDeleteCorrectamente() {
            when(movieRepository.findById(1)).thenReturn(Optional.of(peliculaMock));
            when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

            movieService.deleteMovie(1);

            // La película debe quedar con isActive=false
            assertThat(peliculaMock.getIsActive()).isFalse();
            // Debe guardarse el cambio (no delete físico)
            verify(movieRepository, times(1)).save(peliculaMock);
            verify(movieRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Lanza RuntimeException si la película a inhabilitar no existe")
        void lanzaExcepcionSiPeliculaNoExiste() {
            when(movieRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.deleteMovie(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Película no encontrada con ID: 99");

            verify(movieRepository, never()).save(any(Movie.class));
        }
    }
}