import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router'; // Importar Router
import { AuthService } from '../../../services/auth.service'; // Importar AuthService

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar { 

  // Inyectamos los servicios
  constructor(private authService: AuthService, private router: Router) {}

  cerrarSesion() {
    // 1. Borramos el token del localStorage usando el servicio
    this.authService.logout();

    // 2. Redirigimos a la página principal (pública)
    this.router.navigate(['/']);
    
    console.log("Sesión cerrada. El usuario ahora es un externo.");
  }
}