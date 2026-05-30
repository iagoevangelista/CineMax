import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovieService, Movie } from '../../../services/movie.service';
import { GenreService } from '../../../services/genre.service';
import { ClassificationService } from '../../../services/classification.service';

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movies.html',
  styleUrls: ['./movies.css']
})
export class AdminMovies implements OnInit {
  
  movies: Movie[] = [];
  genres: any[] = [];
  classifications: any[] = [];

  cargando: boolean = true;
  filtroEstado: string = 'Cartelera'; 
  
  isEditMode: boolean = false;
  selectedFile: File | null = null;
  currentMovieId: number | null = null;

  currentMovie: any = {
    titleMovie: '', director: '', synopsis: '', durationMinutes: null,
    releaseDate: '', status: 'Cartelera', premiereWeek: false,
    idClassification: null, idGenres: []
  };

  peliculaDetalle: any = null;

  constructor(
    private movieService: MovieService,
    private genreService: GenreService,
    private classificationService: ClassificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCatalogos();
    this.cargarPeliculas();
  }

  cargarCatalogos() {
    this.classificationService.getAllClassifications().subscribe(res => {
      this.classifications = res;
      this.cdr.detectChanges();
    });
    this.genreService.getAllGenres().subscribe(res => {
      this.genres = res;
      this.cdr.detectChanges();
    });
  }

  cargarPeliculas() {
    this.cargando = true;
    this.movieService.getMoviesByStatus(this.filtroEstado).subscribe({
      next: (res) => {
        this.movies = res;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
      }
    });
  }

  // Se activa cuando cambias el select de Estado
  onEstadoChange() {
    this.cargarPeliculas();
  }

  abrirModalNuevo() {
    this.isEditMode = false;
    this.currentMovieId = null;
    this.selectedFile = null;
    this.currentMovie = {
      titleMovie: '', director: '', synopsis: '', durationMinutes: null,
      releaseDate: '', status: this.filtroEstado, premiereWeek: false,
      idClassification: null, idGenres: []
    };
    this.cdr.detectChanges();
  }

  abrirModalEditar(peli: Movie) {
    this.isEditMode = true;
    this.currentMovieId = peli.idMovie;
    this.selectedFile = null;
    this.currentMovie = { titleMovie: 'Cargando datos...', idGenres: [] };
    this.cdr.detectChanges();

    this.movieService.getMovieById(peli.idMovie).subscribe(detalles => {
      
      // Mapeo seguro contemplando snake_case de la BD
      const classif = this.classifications.find(c => 
        (c.nameClassification || c.name_classification) === detalles.classificationName
      );
      
      const genreIds = detalles.genreNames?.map(name => {
        const found = this.genres.find(g => (g.nameGenre || g.name_genre) === name);
        return found ? (found.idGenre || found.id_genre) : null;
      }).filter(id => id !== null) || [];

      this.currentMovie = {
        titleMovie: detalles.titleMovie,
        director: detalles.director,
        synopsis: detalles.synopsis,
        durationMinutes: detalles.durationMinutes,
        releaseDate: detalles.releaseDate,
        status: detalles.status || 'Cartelera',
        premiereWeek: detalles.premiereWeek || false,
        idClassification: classif ? (classif.idClassification || classif.id_classification) : null,
        idGenres: genreIds
      };
      
      this.cdr.detectChanges(); 
    });
  }

  abrirModalDetalles(peli: Movie) {
    this.peliculaDetalle = null;
    this.cdr.detectChanges();

    this.movieService.getMovieById(peli.idMovie).subscribe(detalles => {
      this.peliculaDetalle = detalles;
      this.cdr.detectChanges();
    });
  }

  onFileSelected(event: any) {
    if (event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  toggleGenre(id: number) {
    const index = this.currentMovie.idGenres.indexOf(id);
    if (index > -1) {
      this.currentMovie.idGenres.splice(index, 1);
    } else {
      this.currentMovie.idGenres.push(id);
    }
  }

  isGenreSelected(id: number): boolean {
    return this.currentMovie.idGenres.includes(id);
  }

  guardarPelicula() {
    const formData = new FormData();
    formData.append('movie', JSON.stringify(this.currentMovie));
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    if (this.isEditMode && this.currentMovieId) {
      this.movieService.updateMovie(this.currentMovieId, formData).subscribe({
        next: () => {
          this.cargarPeliculas();
          alert('Película actualizada correctamente');
        },
        error: (err) => alert('Error al actualizar: ' + err.message)
      });
    } else {
      this.movieService.createMovie(formData).subscribe({
        next: () => {
          this.cargarPeliculas();
          alert('Película creada exitosamente');
        },
        error: (err) => alert('Error al crear: ' + err.message)
      });
    }
  }

  inhabilitarPelicula(id: number) {
    if (confirm('¿Estás seguro de inhabilitar esta película?')) {
      this.movieService.deleteMovie(id).subscribe(() => {
        this.cargarPeliculas();
        alert('Película inhabilitada');
      });
    }
  }
}