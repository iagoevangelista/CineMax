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

  iniciarSesion() {
    if (!this.loginData.email || !this.loginData.password) {
      alert('Por favor ingresa tu correo y contrasena');
      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: (res: any) => {
        // Busca el botón de cerrar del offcanvas específico por su data-bs-dismiss
        const panelLogin = document.getElementById('loginOffcanvas');
        const closeButton = panelLogin?.querySelector('[data-bs-dismiss="offcanvas"]') as HTMLElement;
        closeButton?.click(); // Esto cierra la pantalla de la derecha automáticamente 

        localStorage.setItem('email', this.loginData.email);
        document.getElementById('btn-cerrar-panel')?.click();

        this.verificarSesion(); // Actualiza isLogged y userEmail  [cite: 1, 48, 86-91]

        // Redirección inteligente que ya configuramos  [cite: 1, 45-56]
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
      // Ajusta 'email' al nombre exacto con el que lo guardas en tu localStorage
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
    event.stopPropagation(); // BLOQUEA el error de Bootstrap/Popper
    this.menuAbierto = !this.menuAbierto;
    console.log("¿Menú abierto?", this.menuAbierto);
  }

  // Agrega este listener para cerrar el menú si haces clic en cualquier otro lado de la pantalla
  @HostListener('document:click', ['$event'])
  clickFuera(event: any) {
    this.menuAbierto = false;
  }
}
