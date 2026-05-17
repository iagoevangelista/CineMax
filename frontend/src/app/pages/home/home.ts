import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MovieService, Movie } from '../../services/movie.service';
import { finalize, delay } from 'rxjs/operators';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 



@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', email: '', password: '', documentNumber: '', idDocumentType: '1' };

  movies: Movie[] = [];
  filteredMovies: Movie[] = [];
  loading = true;
  error = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private movieService: MovieService,
    private cdr: ChangeDetectorRef 
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
    this.authService.register(this.registerData).subscribe({
      next: () => {
        // Como authService.ts ya guardó el token, el usuario ya está "logueado"
        document.getElementById('btn-cerrar-panel')?.click(); // Cerramos el modal
        
        // Recargamos o redirigimos según lo que quieras
        alert('¡Bienvenido a CineMax!');
        window.location.reload(); 
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

vistaActiva: string = 'login'; 

  cambiarVista(vista: string, event?: Event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    this.vistaActiva = vista;
  }

  // Variables para la experiencia de usuario
  correoRecuperacion: string = '';
  loadingRecuperacion: boolean = false;
  mensajeRecuperacion: { texto: string, tipo: 'success' | 'danger' } | null = null;

  solicitarRecuperacion() {
    this.mensajeRecuperacion = null;

    if (!this.correoRecuperacion) {
      this.mensajeRecuperacion = { texto: 'Por favor ingresa tu correo corporativo.', tipo: 'danger' };
      return;
    }

    // 1. Encendemos el spinner
    this.loadingRecuperacion = true;
    
    // 2. FORZAMOS A ANGULAR A DIBUJAR EL SPINNER AHORA MISMO
    this.cdr.detectChanges(); 

    this.authService.forgotPassword(this.correoRecuperacion)
      .pipe(
        // 3. Retenemos la respuesta por 9 segundos exactos
        delay(9000), 
        finalize(() => this.loadingRecuperacion = false) 
      )
      .subscribe({
        next: (res: any) => {
          this.mensajeRecuperacion = { 
            texto: res.message || '¡Enlace enviado! Revisa tu bandeja de entrada.', 
            tipo: 'success' 
          };
          this.correoRecuperacion = '';  
          this.cdr.detectChanges();
          
          setTimeout(() => {
            this.mensajeRecuperacion = null;
            this.cdr.detectChanges(); 
          }, 8000);
        },
        error: (err: any) => {
          console.error("Error en recuperación:", err);
          const msg = err.error?.error || err.error?.message || 'Hubo un error al conectar con el servidor.';
          this.mensajeRecuperacion = { texto: msg, tipo: 'danger' };
        }
      });
  }

}