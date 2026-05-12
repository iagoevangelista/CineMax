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

  peliculasFiltradas: Movie[] = [];
  loading = false;
  error = false;

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
    this.peliculasFiltradas = [];
    this.cdr.detectChanges();

    this.movieService.getMoviesByStatus(tab as any).subscribe({
      next: (data: Movie[]) => {
        console.log('✅ Datos del backend:', data);
        this.peliculasFiltradas = [...data];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        console.log('❌ Error al cargar películas');
        this.error = true;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filtrar(tab: string) {
    this.cargarPorTab(tab);
  }
}