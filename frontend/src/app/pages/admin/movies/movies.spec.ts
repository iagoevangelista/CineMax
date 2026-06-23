import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { AdminMovies } from './movies';
import { MovieService } from '../../../services/movie.service';
import { GenreService } from '../../../services/genre.service';
import { ClassificationService } from '../../../services/classification.service';

/**
 * Pruebas unitarias del componente AdminMovies (HU-13).
 *
 * Estrategia: TestBed mínimo con mocks de los 3 servicios.
 * Se prueban:
 *   - Inicialización del FormGroup (campos y validadores)
 *   - Validaciones de cada campo (required, minlength, maxlength, min, max)
 *   - Lógica de negocio: invalido(), sinGeneros, toggleGenre(), isGenreSelected()
 *   - Integración con servicios: cargarPeliculas(), guardarPelicula()
 */
describe('AdminMovies (HU-13)', () => {

  let component: AdminMovies;
  let fixture: ComponentFixture<AdminMovies>;

  let mockMovieService: jasmine.SpyObj<MovieService>;
  let mockGenreService: jasmine.SpyObj<GenreService>;
  let mockClassificationService: jasmine.SpyObj<ClassificationService>;

  const peliculasMock = [
    { idMovie: 1, titleMovie: 'Inception', director: 'Nolan', synopsis: 'A mind-bending thriller', posterUrl: 'inception.jpg', durationMinutes: 148, releaseDate: '2010-07-16', status: 'Cartelera', premiereWeek: false },
    { idMovie: 2, titleMovie: 'Dune', director: 'Villeneuve', synopsis: 'Epic sci-fi adventure', posterUrl: 'dune.jpg', durationMinutes: 155, releaseDate: '2021-10-22', status: 'Cartelera', premiereWeek: true },
  ];

  const generosMock = [
    { idGenre: 1, nameGenre: 'Acción' },
    { idGenre: 2, nameGenre: 'Drama' },
    { idGenre: 3, nameGenre: 'Ciencia Ficción' },
  ];

  const clasificacionesMock = [
    { idClassification: 1, nameClassification: 'PG-13' },
    { idClassification: 2, nameClassification: 'R' },
  ];

  beforeEach(async () => {
    mockMovieService = jasmine.createSpyObj('MovieService', [
      'getMoviesByStatus', 'getMovieById', 'createMovie', 'updateMovie', 'deleteMovie'
    ]);
    mockGenreService = jasmine.createSpyObj('GenreService', ['getAllGenres']);
    mockClassificationService = jasmine.createSpyObj('ClassificationService', ['getAllClassifications']);

    // Respuestas por defecto
    mockMovieService.getMoviesByStatus.and.returnValue(of(peliculasMock));
    mockGenreService.getAllGenres.and.returnValue(of(generosMock));
    mockClassificationService.getAllClassifications.and.returnValue(of(clasificacionesMock));

    await TestBed.configureTestingModule({
      imports: [AdminMovies, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MovieService, useValue: mockMovieService },
        { provide: GenreService, useValue: mockGenreService },
        { provide: ClassificationService, useValue: mockClassificationService },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMovies);
    component = fixture.componentInstance;
    fixture.detectChanges(); // dispara ngOnInit
  });

  // ── Inicialización ─────────────────────────────────────────────────────────

  describe('Inicialización', () => {

    it('debe crear el componente correctamente', () => {
      expect(component).toBeTruthy();
    });

    it('debe llamar a getMoviesByStatus, getAllGenres y getAllClassifications al inicializarse', () => {
      expect(mockMovieService.getMoviesByStatus).toHaveBeenCalledWith('Cartelera');
      expect(mockGenreService.getAllGenres).toHaveBeenCalledTimes(1);
      expect(mockClassificationService.getAllClassifications).toHaveBeenCalledTimes(1);
    });

    it('debe cargar la lista de películas desde el servicio', () => {
      expect(component.movies.length).toBe(2);
      expect(component.movies[0].titleMovie).toBe('Inception');
    });

    it('debe construir el FormGroup con todos los campos esperados', () => {
      expect(component.form).toBeTruthy();
      ['titleMovie', 'director', 'synopsis', 'durationMinutes',
        'releaseDate', 'status', 'premiereWeek', 'idClassification', 'idGenres'
      ].forEach(campo => {
        expect(component.form.contains(campo)).toBeTrue();
      });
    });

    it('debe inicializar el formulario como inválido (campos requeridos vacíos)', () => {
      component.abrirModalNuevo();
      expect(component.form.valid).toBeFalse();
    });
  });

  // ── Validaciones del FormGroup ─────────────────────────────────────────────

  describe('FormGroup — validaciones', () => {

    beforeEach(() => {
      component.abrirModalNuevo();
    });

    it('CP-MOV-01: titleMovie vacío hace el campo inválido (required)', () => {
      component.form.patchValue({ titleMovie: '' });
      expect(component.form.get('titleMovie')?.hasError('required')).toBeTrue();
    });

    it('titleMovie con 1 carácter viola minlength(2)', () => {
      component.form.patchValue({ titleMovie: 'A' });
      expect(component.form.get('titleMovie')?.hasError('minlength')).toBeTrue();
    });

    it('titleMovie con 101 caracteres viola maxlength(100)', () => {
      component.form.patchValue({ titleMovie: 'A'.repeat(101) });
      expect(component.form.get('titleMovie')?.hasError('maxlength')).toBeTrue();
    });

    it('titleMovie con 2+ caracteres válidos hace el campo válido', () => {
      component.form.patchValue({ titleMovie: 'Inception' });
      expect(component.form.get('titleMovie')?.valid).toBeTrue();
    });

    it('CP-MOV-02: director vacío hace el campo inválido (required)', () => {
      component.form.patchValue({ director: '' });
      expect(component.form.get('director')?.hasError('required')).toBeTrue();
    });

    it('director con 101 caracteres viola maxlength(100)', () => {
      component.form.patchValue({ director: 'D'.repeat(101) });
      expect(component.form.get('director')?.hasError('maxlength')).toBeTrue();
    });

    it('CP-MOV-03: durationMinutes vacío hace el campo inválido (required)', () => {
      component.form.patchValue({ durationMinutes: null });
      expect(component.form.get('durationMinutes')?.hasError('required')).toBeTrue();
    });

    it('durationMinutes en 0 viola min(1)', () => {
      component.form.patchValue({ durationMinutes: 0 });
      expect(component.form.get('durationMinutes')?.hasError('min')).toBeTrue();
    });

    it('durationMinutes en 301 viola max(300)', () => {
      component.form.patchValue({ durationMinutes: 301 });
      expect(component.form.get('durationMinutes')?.hasError('max')).toBeTrue();
    });

    it('durationMinutes en 120 es válido', () => {
      component.form.patchValue({ durationMinutes: 120 });
      expect(component.form.get('durationMinutes')?.valid).toBeTrue();
    });

    it('CP-MOV-04: idClassification en null hace el campo inválido (required)', () => {
      component.form.patchValue({ idClassification: null });
      expect(component.form.get('idClassification')?.hasError('required')).toBeTrue();
    });

    it('synopsis con 501 caracteres viola maxlength(500)', () => {
      component.form.patchValue({ synopsis: 'S'.repeat(501) });
      expect(component.form.get('synopsis')?.hasError('maxlength')).toBeTrue();
    });

    it('formulario completo con datos válidos queda válido (con al menos un género)', () => {
      component.form.patchValue({
        titleMovie: 'Inception', director: 'Christopher Nolan',
        synopsis: 'Un sueño dentro de un sueño.', durationMinutes: 148,
        releaseDate: '2010-07-16', status: 'Cartelera',
        premiereWeek: false, idClassification: 1, idGenres: [1]
      });
      expect(component.form.valid).toBeTrue();
    });
  });

  // ── invalido() ─────────────────────────────────────────────────────────────

  describe('invalido()', () => {

    it('devuelve false si el campo es válido y el formulario no se envió', () => {
      component.form.patchValue({ titleMovie: 'Inception' });
      expect(component.invalido('titleMovie')).toBeFalse();
    });

    it('devuelve true si el campo es inválido y formEnviado es true', () => {
      component.abrirModalNuevo();
      component.form.patchValue({ titleMovie: '' });
      component.formEnviado = true;
      expect(component.invalido('titleMovie')).toBeTrue();
    });

    it('devuelve true si el campo fue touched y tiene error', () => {
      component.form.get('titleMovie')?.markAsTouched();
      component.form.patchValue({ titleMovie: '' });
      expect(component.invalido('titleMovie')).toBeTrue();
    });
  });

  // ── sinGeneros ─────────────────────────────────────────────────────────────

  describe('sinGeneros', () => {

    it('devuelve true cuando idGenres está vacío', () => {
      component.form.patchValue({ idGenres: [] });
      expect(component.sinGeneros).toBeTrue();
    });

    it('devuelve false cuando hay al menos un género seleccionado', () => {
      component.form.patchValue({ idGenres: [1] });
      expect(component.sinGeneros).toBeFalse();
    });
  });

  // ── toggleGenre() e isGenreSelected() ──────────────────────────────────────

  describe('toggleGenre() e isGenreSelected()', () => {

    beforeEach(() => {
      component.abrirModalNuevo(); // idGenres = []
    });

    it('agrega el género al array si no estaba seleccionado', () => {
      component.toggleGenre(1);
      expect(component.isGenreSelected(1)).toBeTrue();
    });

    it('elimina el género del array si ya estaba seleccionado', () => {
      component.form.patchValue({ idGenres: [1, 2] });
      component.toggleGenre(1);
      expect(component.isGenreSelected(1)).toBeFalse();
      expect(component.isGenreSelected(2)).toBeTrue();
    });

    it('puede seleccionar múltiples géneros independientemente', () => {
      component.toggleGenre(1);
      component.toggleGenre(3);
      expect(component.isGenreSelected(1)).toBeTrue();
      expect(component.isGenreSelected(2)).toBeFalse();
      expect(component.isGenreSelected(3)).toBeTrue();
    });
  });

  // ── guardarPelicula() ──────────────────────────────────────────────────────

  describe('guardarPelicula()', () => {

    beforeEach(() => {
      component.abrirModalNuevo();
    });

    it('no llama al servicio si el formulario es inválido', () => {
      // Formulario vacío (inválido)
      component.guardarPelicula();
      expect(mockMovieService.createMovie).not.toHaveBeenCalled();
    });

    it('no llama al servicio si el formulario es válido pero sinGeneros es true', () => {
      component.form.patchValue({
        titleMovie: 'Inception', director: 'Nolan', durationMinutes: 148,
        status: 'Cartelera', idClassification: 1, idGenres: []
      });
      component.guardarPelicula();
      expect(mockMovieService.createMovie).not.toHaveBeenCalled();
    });

    it('activa formEnviado = true al intentar guardar con formulario inválido', () => {
      component.formEnviado = false;
      component.guardarPelicula();
      expect(component.formEnviado).toBeTrue();
    });

    it('llama a createMovie en modo creación con formulario válido', () => {
      mockMovieService.createMovie.and.returnValue(of({}));
      component.form.patchValue({
        titleMovie: 'Inception', director: 'Christopher Nolan',
        synopsis: '', durationMinutes: 148, releaseDate: '',
        status: 'Cartelera', premiereWeek: false,
        idClassification: 1, idGenres: [1]
      });
      component.guardarPelicula();
      expect(mockMovieService.createMovie).toHaveBeenCalledTimes(1);
    });

    it('llama a updateMovie en modo edición con formulario válido', () => {
      mockMovieService.updateMovie.and.returnValue(of({}));
      component.isEditMode = true;
      component.currentMovieId = 1;
      component.form.patchValue({
        titleMovie: 'Inception', director: 'Christopher Nolan',
        synopsis: '', durationMinutes: 148, releaseDate: '',
        status: 'Cartelera', premiereWeek: false,
        idClassification: 1, idGenres: [1]
      });
      component.guardarPelicula();
      expect(mockMovieService.updateMovie).toHaveBeenCalledWith(1, jasmine.any(FormData));
    });
  });

  // ── abrirModalNuevo() ──────────────────────────────────────────────────────

  describe('abrirModalNuevo()', () => {

    it('resetea isEditMode a false y currentMovieId a null', () => {
      component.isEditMode = true;
      component.currentMovieId = 5;
      component.abrirModalNuevo();
      expect(component.isEditMode).toBeFalse();
      expect(component.currentMovieId).toBeNull();
    });

    it('resetea formEnviado a false', () => {
      component.formEnviado = true;
      component.abrirModalNuevo();
      expect(component.formEnviado).toBeFalse();
    });

    it('resetea idGenres a array vacío', () => {
      component.form.patchValue({ idGenres: [1, 2] });
      component.abrirModalNuevo();
      expect(component.form.get('idGenres')?.value).toEqual([]);
    });
  });

  // ── cargarPeliculas() ──────────────────────────────────────────────────────

  describe('cargarPeliculas()', () => {

    it('vuelve a llamar al servicio con el filtroEstado actual', () => {
      component.filtroEstado = 'Próximamente';
      mockMovieService.getMoviesByStatus.and.returnValue(of([]));
      component.cargarPeliculas();
      expect(mockMovieService.getMoviesByStatus).toHaveBeenCalledWith('Próximamente');
    });

    it('pone cargando = false si el servicio responde con error', () => {
      mockMovieService.getMoviesByStatus.and.returnValue(throwError(() => new Error('fallo')));
      component.cargarPeliculas();
      expect(component.cargando).toBeFalse();
    });
  });
});