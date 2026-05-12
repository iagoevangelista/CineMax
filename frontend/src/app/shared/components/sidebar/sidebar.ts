import { Component, OnInit } from '@angular/core'; // 1. Agregamos OnInit aquí
import { RouterModule, Router } from '@angular/router'; 
import { AuthService } from '../../../services/auth.service'; 
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar implements OnInit { // 2. Agregamos "implements OnInit"

  // 3. Creamos la variable para el rol
  rolUsuario: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  // 4. Implementamos el método ngOnInit para leer el rol apenas cargue el sidebar
  ngOnInit(): void {
    this.rolUsuario = this.authService.getRole();
    console.log("Rol detectado en Sidebar:", this.rolUsuario);
  }

  cerrarSesion() {
    this.authService.logout();
    this.router.navigate(['/']);
    console.log("Sesión cerrada. El usuario ahora es un externo.");
  }
}
