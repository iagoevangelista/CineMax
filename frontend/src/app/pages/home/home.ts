import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service'; // <-- Asegúrate de importar esto
import { MovieService, Movie } from '../../services/movie.service';
import { finalize, delay } from 'rxjs/operators';

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

  // --- NUEVAS VARIABLES PARA EL PANEL DE PERFIL LATERAL ---
  isLogged: boolean = false;
  userEmail: string = '';
  menuAbierto: boolean = false;
  vistaActiva: string = 'login'; 

  datosUserAccount: any = {}; // Guardará los datos inmutables de la BD
  formPerfil = {
    firstName: '', lastName: '', phone: '', datebirth: '', oldPassword: '', newPassword: ''
  };
  mensajePerfil: string = '';
  mensajeAuth: string = '';
  esErrorAuth: boolean = false;
  esErrorPerfil: boolean = false;
  fechaMaxima: string = new Date().toISOString().split('T')[0];

  movies: Movie[] = [];
  filteredMovies: Movie[] = [];
  loading = true;
  error = false;

  constructor(
    private authService: AuthService,
    private userService: UserService, // <-- Inyectamos el servicio de usuarios
    private router: Router,
    private movieService: MovieService,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    this.isLogged = this.authService.isLoggedIn();
    if (this.isLogged) {
      this.userEmail = this.authService.getEmail() || 'Usuario';
      this.cargarDatosPerfilLateral(); // Cargamos sus datos si ya inició sesión
    }
    this.loadMovies();
  }

  // Métodos de control del menú superior de sesión
  toggleMenu(event: Event) {
    event.stopPropagation();
    this.menuAbierto = !this.menuAbierto;
  }

  cerrarSesion() {
    this.authService.logout();
    this.isLogged = false;
    this.menuAbierto = false;
    alert('Sesión cerrada correctamente.');
    window.location.reload();
  }

  // --- LÓGICA DE OPERACIÓN DEL PANEL DE PERFIL LATERAL ---
  cargarDatosPerfilLateral() {
    this.userService.getProfile().subscribe({
      next: (res) => {
        this.datosUserAccount = res;
        this.formPerfil.firstName = res.firstName;
        this.formPerfil.lastName = res.lastName;
        this.formPerfil.phone = res.phone || '';
        this.formPerfil.datebirth = res.datebirth || '';
      },
      error: (err) => console.error('Error al precargar el perfil lateral:', err)
    });
  }

  guardarCambiosPerfilLateral() {
    this.mensajePerfil = '';
    
    // Si los campos de password están vacíos, los enviamos en blanco
    const payload = { ...this.formPerfil };
    if (!payload.newPassword) payload.newPassword = '';
    if (!payload.oldPassword) payload.oldPassword = '';

    this.userService.updateProfile(payload).subscribe({
      next: (res: any) => {
        this.esErrorPerfil = false;
        this.mensajePerfil = res.message || '¡Perfil actualizado con éxito!';
        this.formPerfil.oldPassword = '';
        this.formPerfil.newPassword = '';
        this.cargarDatosPerfilLateral(); // Refrescar info inmutable
        
        // ¡LA SOLUCIÓN! Le decimos a Angular que refresque la alerta inmediatamente
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        this.esErrorPerfil = true;
        this.mensajePerfil = err.error?.message || 'Error al intentar actualizar los datos.';
        
        // ¡LA SOLUCIÓN! Le decimos a Angular que refresque la alerta inmediatamente
        this.cdr.detectChanges(); 
      }
    });
  }
  
  // Espacio listo para añadir la lógica que me pasarás luego
  eliminarCuenta() {
    const confirmar = confirm('¿Estás completamente seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.');
    if (confirmar) {
      alert('Funcionalidad de eliminación en desarrollo según tus especificaciones futuras.');
    }
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
    this.mensajeAuth = ''; // Limpiamos mensajes anteriores
    
    if (!this.loginData.email || !this.loginData.password) {
      this.esErrorAuth = true;
      this.mensajeAuth = 'Por favor ingresa tu correo y contraseña.';
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: () => {
        this.esErrorAuth = false;
        this.mensajeAuth = '¡Sesión iniciada con éxito! Entrando...';
        
        // Le damos 1.5 segundos para que el usuario lea el mensaje verde antes de cerrar
        setTimeout(() => {
          document.getElementById('btn-cerrar-panel')?.click();
          this.isLogged = true;
          this.userEmail = this.authService.getEmail() || this.loginData.email;
          this.loginData = { email: '', password: '' };
          this.mensajeAuth = '';
          this.cargarDatosPerfilLateral();
        }, 1500); 
      },
      error: (err: any) => {
        this.esErrorAuth = true;
        this.mensajeAuth = 'Correo o contraseña incorrectos.';
      }
    });
  }

  registrarse() {
    this.mensajeAuth = '';
    
    this.authService.register(this.registerData).subscribe({
      next: () => {
        this.esErrorAuth = false;
        this.mensajeAuth = '¡Registro exitoso! Redirigiendo...';
        
        // Le damos 2 segundos para que lea el éxito y recargamos
        setTimeout(() => {
          document.getElementById('btn-cerrar-panel')?.click(); 
          window.location.reload(); 
        }, 2000);
      },
      error: (err: any) => {
        this.esErrorAuth = true;
        this.mensajeAuth = err.error?.message || 'Error al registrarse. Asegúrate de cumplir los requisitos.';
      }
    });
  }

irAMovies() {
  this.router.navigate(['/movies']);
}
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