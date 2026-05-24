import { Component, OnInit } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MovieService, Movie } from '../../services/movie.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  movies: Movie[] = [];
  filteredMovies: Movie[] = [];
  loading = true;
  error = false;

  constructor(
    private router: Router,
    private movieService: MovieService
  ) {}

  ngOnInit() {
    this.loadMovies();
  }

  loadMovies() {
    this.loading = true;
    this.error = false;
    this.movieService.getMovies().subscribe({
      next: (data: Movie[]) => {
        this.movies = data;
        this.filteredMovies = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error cargando peliculas:', err);
        this.error = true;
        this.loading = false;
      }
    });
  }

  irAMovies() {
    this.router.navigate(['/movies']);
  }
}