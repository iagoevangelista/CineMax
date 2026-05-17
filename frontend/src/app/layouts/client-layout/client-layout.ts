import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './client-layout.html',
  styleUrl: './client-layout.css',
})
export class ClientLayout implements OnInit{

  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', idDocumentType: 1, documentNumber: '', email: '', password: '' };
  isLogged: boolean = false;
  userEmail: string = '';
  menuAbierto: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.verificarSesion();
  }

  vistaActiva: string = 'login'; 

  cambiarVista(vista: string, event?: Event) {
    console.log('Cambiando a:', vista);
    this.vistaActiva = vista;
  }

  iniciarSesion() {
    if (!this.loginData.email || !this.loginData.password) {
      alert('Por favor ingresa tu correo y contrasena');
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        const panelLogin = document.getElementById('loginOffcanvas');
        const closeButton = panelLogin?.querySelector('[data-bs-dismiss="offcanvas"]') as HTMLElement;
        closeButton?.click(); 

        localStorage.setItem('email', this.loginData.email);
        document.getElementById('btn-cerrar-panel')?.click();

        this.verificarSesion(); 

        const rolUsuario = this.authService.getRole(); 
        if (rolUsuario && rolUsuario.toUpperCase() === 'CLIENTE') {
          if (this.router.url.includes('/seats') || this.router.url.includes('/tickets')) {
             console.log("Sesión iniciada. Manteniendo posición en compra.");
          } else {
             this.router.navigate(['/']); 
          }
        } else {
          this.router.navigate(['/admin/dashboard']); 
        }
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

  correoRecuperacion: string = '';

  solicitarRecuperacion() {
    if (!this.correoRecuperacion) {
      alert('Por favor ingresa tu correo corporativo.');
      return;
    }

    this.authService.forgotPassword(this.correoRecuperacion).subscribe({
      next: (res: any) => {
        alert(res.message || 'Si el correo existe, se ha enviado un enlace de recuperación.');
        this.cambiarVista('login'); // Regresamos a la vista normal
        this.correoRecuperacion = ''; // Limpiamos el campo
      },
      error: (err: any) => {
        console.error("Error al solicitar recuperación:", err);
        // Mostramos el error del backend o uno genérico
        const msg = err.error?.error || 'Hubo un error al procesar la solicitud.';
        alert(msg);
      }
    });
  }

  verificarSesion() {
    this.isLogged = this.authService.isLoggedIn();
    if (this.isLogged) {
      this.userEmail = localStorage.getItem('email') || 'Mi Cuenta'; 
    }
  }

  cerrarSesion() {
    this.menuAbierto = false; 
    this.authService.logout();
    this.isLogged = false;
    this.router.navigate(['/']);
  }

  toggleMenu(event: Event) {
    event.preventDefault();
    event.stopPropagation(); 
    this.menuAbierto = !this.menuAbierto;
  }

  @HostListener('document:click', ['$event'])
  clickFuera(event: any) {
    this.menuAbierto = false;
  }
}