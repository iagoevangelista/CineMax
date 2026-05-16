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
  registerData = { firstName: '', lastName: '', email: '', password: '' };
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
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
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