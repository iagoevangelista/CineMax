import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MovieService, Movie } from '../../services/movie.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', email: '', password: '' };

  movies: Movie[] = [];
  filteredMovies: Movie[] = [];
  loading = true;
  error = false;

  constructor(
    private authService: AuthService,
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

  iniciarSesion() {
    if (!this.loginData.email || !this.loginData.password) {
      alert('Por favor ingresa tu correo y contrasena');
      return;
    }
    this.authService.login(this.loginData).subscribe({
      next: () => {
        document.getElementById('cerrarLogin')?.click();
        this.router.navigate(['/admin/dashboard']);
      },
      error: (err: any) => {
        console.error('Error al iniciar sesion:', err);
        alert('Correo o contrasena incorrectos');
      }
    });
  }

  registrarse() {
    if (!this.registerData.firstName || !this.registerData.email || !this.registerData.password) {
      alert('Por favor completa todos los campos');
      return;
    }
    this.authService.register(this.registerData).subscribe({
      next: () => {
        alert('Registro exitoso. Ya puedes iniciar sesion.');
        document.getElementById('cerrarRegister')?.click();
      },
      error: (err: any) => {
        console.error('Error al registrarse:', err);
        alert('Error al registrarse. Intenta de nuevo.');
      }
    });
  }

irAMovies() {
  this.router.navigate(['/movies']);
}
}