import { Component, OnInit, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service'; 
import { finalize, delay } from 'rxjs/operators';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './client-layout.html',
  styleUrl: './client-layout.css',
})
export class ClientLayout implements OnInit {

  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', idDocumentType: 1, documentNumber: '', email: '', password: '' };
  
  isLogged: boolean = false;
  userEmail: string = '';
  menuAbierto: boolean = false;
  vistaActiva: string = 'login'; 

  userFullName: string = 'Usuario';
  userInitials: string = 'U';
  userImageUrl: string | null = null; 

  mostrarPassword = false;
  mostrarPasswordReg = false;
  cargandoLogin: boolean = false;

  mensajeAuth: string = '';
  esErrorAuth: boolean = false;
  correoRecuperacion: string = '';
  loadingRecuperacion: boolean = false;
  mensajeRecuperacion: { texto: string, tipo: 'success' | 'danger' } | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    this.verificarSesion();

    // MAGIA: Escuchamos en vivo cualquier cambio que sufra el usuario
    this.userService.user$.subscribe(user => {
      if (user) {
        this.userFullName = `${user.firstName} ${user.lastName}`.trim();
        this.userInitials = this.obtenerIniciales(user.firstName, user.lastName);
        this.userImageUrl = user.imageUrl || null; // Carga la URL si existe
        this.cdr.detectChanges(); // Fuerza a pintar la foto arriba
      }
    });
  }

  cargarDatosUsuario() {
    // Al pedir el perfil, nuestro servicio ahora dispara el user$ automáticamente
    this.userService.getProfile().subscribe({
      error: (err) => console.error('Error al cargar perfil rápido:', err)
    });
  }

  obtenerIniciales(nombre: string, apellido: string): string {
    const n = nombre ? nombre.charAt(0).toUpperCase() : '';
    const a = apellido ? apellido.charAt(0).toUpperCase() : '';
    return n + a || 'U'; 
  }

  cambiarVista(vista: string, event?: Event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    this.vistaActiva = vista;
    this.mensajeAuth = ''; 
    this.cdr.detectChanges();
  }

  toggleMostrarPassword() {
    this.mostrarPassword = !this.mostrarPassword;
    this.cdr.detectChanges();
  }

  toggleMostrarPasswordReg() {
    this.mostrarPasswordReg = !this.mostrarPasswordReg;
    this.cdr.detectChanges();
  }

  iniciarSesion() {
    this.mensajeAuth = ''; 
    if (!this.loginData.email || !this.loginData.password) {
      this.esErrorAuth = true;
      this.mensajeAuth = 'Por favor ingresa tu correo y contraseña.';
      this.cdr.detectChanges();
      return;
    }

    this.cargandoLogin = true;
    this.cdr.detectChanges();

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        this.esErrorAuth = false;
        this.mensajeAuth = '¡Sesión iniciada con éxito! Entrando...';
        this.cargandoLogin = false;
        this.cdr.detectChanges();
        
        setTimeout(() => {
          document.getElementById('btn-cerrar-panel')?.click();
          this.verificarSesion(); 
          this.loginData = { email: '', password: '' };
          this.mensajeAuth = '';
          this.cdr.detectChanges();

          const rolUsuario = this.authService.getRole(); 
          if (rolUsuario === 'ADMIN' || (rolUsuario && rolUsuario.toUpperCase().startsWith('GERENTE'))) {
            this.router.navigate(['/admin/dashboard']); 
          } else {
            if (!this.router.url.includes('/seats') && !this.router.url.includes('/tickets')) {
               if(this.router.url === '/') window.location.reload();
            }
          }
        }, 1500);
      },
      error: (err: any) => {
        this.cargandoLogin = false;
        this.esErrorAuth = true;
        this.mensajeAuth = 'Correo o contraseña incorrectos.';
        this.cdr.detectChanges();
      }
    });
  }

  registrarse() {
    this.mensajeAuth = '';
    if (!this.registerData.firstName || !this.registerData.email || !this.registerData.password) {
      this.esErrorAuth = true;
      this.mensajeAuth = 'Por favor completa todos los campos obligatorios.';
      this.cdr.detectChanges();
      return;
    }

    this.authService.register(this.registerData).subscribe({
      next: () => {
        this.esErrorAuth = false;
        this.mensajeAuth = '¡Registro exitoso! Redirigiendo al login...';
        this.cdr.detectChanges();
        
        setTimeout(() => {
          this.cambiarVista('login');
        }, 2000);
      },
      error: (err: any) => {
        this.esErrorAuth = true;
        this.mensajeAuth = err.error?.message || 'Error al registrarse. Intenta de nuevo.';
        this.cdr.detectChanges();
      }
    });
  }

  solicitarRecuperacion() {
    this.mensajeRecuperacion = null;
    if (!this.correoRecuperacion) {
      this.mensajeRecuperacion = { texto: 'Por favor ingresa tu correo.', tipo: 'danger' };
      this.cdr.detectChanges();
      return;
    }

    this.loadingRecuperacion = true;
    this.cdr.detectChanges(); 

    this.authService.forgotPassword(this.correoRecuperacion)
      .pipe(
        delay(3000), 
        finalize(() => {
          this.loadingRecuperacion = false;
          this.cdr.detectChanges();
        }) 
      )
      .subscribe({
        next: (res: any) => {
          this.mensajeRecuperacion = { 
            texto: res.message || '¡Enlace enviado! Revisa tu bandeja de entrada.', 
            tipo: 'success' 
          };
          this.correoRecuperacion = '';  
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          const msg = err.error?.error || 'Hubo un error al conectar con el servidor.';
          this.mensajeRecuperacion = { texto: msg, tipo: 'danger' };
          this.cdr.detectChanges();
        }
      });
  }

  verificarSesion() {
    this.isLogged = this.authService.isLoggedIn();
    if (this.isLogged) {
      this.userEmail = this.authService.getEmail() || localStorage.getItem('email') || 'Mi Cuenta'; 
      this.cargarDatosUsuario(); 
    }
    this.cdr.detectChanges();
  }

  cerrarSesion() {
    this.menuAbierto = false; 
    this.authService.logout();
    this.isLogged = false;
    this.cdr.detectChanges();
    alert('Sesión cerrada correctamente.');
    window.location.reload();
  }

  toggleMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation(); 
    this.menuAbierto = !this.menuAbierto;
    this.cdr.detectChanges();
  }

  @HostListener('document:click', ['$event'])
  clickFuera(event: any) {
    if (this.menuAbierto) {
      this.menuAbierto = false;
      this.cdr.detectChanges();
    }
  }
}