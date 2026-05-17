import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MovieService, Movie } from '../../services/movie.service';

@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './movies.html',
  styleUrl: './movies.css'
})
export class Movies implements OnInit {

  tabActivo: string = 'Cartelera';
  tabs = ['Cartelera', 'Estreno', 'Preventa'];

  todasLasPeliculas: Movie[] = [];
  peliculasFiltradas: Movie[] = [];
  loading = false;
  error = false;

  textoBusqueda: string = '';
  clasificacionSeleccionada: string = '';
  clasificaciones: string[] = ['ATP', '+14', '+17', 'PG-13'];
  mensajeFiltro: string = '';

  constructor(
    private movieService: MovieService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.cargarPorTab('Cartelera');
  }

  cargarPorTab(tab: string) {
    this.tabActivo = tab;
    this.loading = true;
    this.error = false;
    this.todasLasPeliculas = [];
    this.peliculasFiltradas = [];
    this.textoBusqueda = '';
    this.clasificacionSeleccionada = '';
    this.mensajeFiltro = '';
    this.cdr.detectChanges();

    if (tab === 'Cartelera') {
      this.movieService.getMoviesByStatus('Cartelera').subscribe({
        next: (cartelera) => {
          this.movieService.getMoviesByStatus('Estreno').subscribe({
            next: (estrenos) => {
              this.todasLasPeliculas = [...cartelera, ...estrenos];
              this.peliculasFiltradas = [...this.todasLasPeliculas];
              this.loading = false;
              this.cdr.detectChanges();
            },
            error: () => {
              this.todasLasPeliculas = [...cartelera];
              this.peliculasFiltradas = [...this.todasLasPeliculas];
              this.loading = false;
              this.cdr.detectChanges();
            }
          });
        },
        error: () => {
          this.error = true;
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.movieService.getMoviesByStatus(tab as any).subscribe({
        next: (data: Movie[]) => {
          this.todasLasPeliculas = [...data];
          this.peliculasFiltradas = [...this.todasLasPeliculas];
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.error = true;
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  onBusqueda(valor: string) {
    this.textoBusqueda = valor;
    this.aplicarFiltros();
  }

  onClasificacion(valor: string) {
    this.clasificacionSeleccionada = valor;
    this.aplicarFiltros();
  }

  aplicarFiltros() {
    const texto = this.textoBusqueda.trim().toLowerCase();
    const clasif = this.clasificacionSeleccionada;

    this.peliculasFiltradas = this.todasLasPeliculas.filter(p => {
      const coincideTexto = texto === '' || p.titleMovie.toLowerCase().includes(texto);
      const coincideClasif = clasif === '' || (p as any).rating === clasif;
      return coincideTexto && coincideClasif;
    });

    if (this.peliculasFiltradas.length === 0) {
      this.mensajeFiltro = 'No se encontraron películas con esos filtros.';
    } else {
      this.mensajeFiltro = `${this.peliculasFiltradas.length} película(s) encontrada(s).`;
    }
  }

  limpiarFiltros() {
    this.textoBusqueda = '';
    this.clasificacionSeleccionada = '';
    this.mensajeFiltro = '';
    this.peliculasFiltradas = [...this.todasLasPeliculas];
  }

  irADetalle(id: number) {
    window.location.href = '/movie/' + id;
  }
}