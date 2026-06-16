import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-navbar-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-admin.html',
  styleUrl: './navbar-admin.css'
})
export class NavbarAdmin implements OnInit {

  rolActual: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.rolActual = this.authService.getRole() || '';
  }

  /* Convierte el roleName de la BD a un texto legible para mostrar en pantalla.
   * Ejemplo: "GERENTE_DE_OPERACIONES" a "Gerente De Operaciones" */
  
  formatearRol(roleName: string): string {
    if (!roleName) return '';
    return roleName
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, letra => letra.toUpperCase());
  }
}
