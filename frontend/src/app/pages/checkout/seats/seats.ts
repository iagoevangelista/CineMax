import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BookingService } from '../../../services/booking';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-seats',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './seats.html',
  styleUrl: './seats.css'
})
export class Seats implements OnInit {
  // === VARIABLES DE ASIENTOS ===
  filas = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
  columnas = [1, 2, 3, 4, 5, 6, 7, 8];
  asientosOcupados = ['A4', 'B8', 'F6', 'F7', 'F8'];
  asientosDiscapacitados = ['B3', 'B4'];
  asientosSeleccionados: string[] = [];
  resumenActual: any;

  // === VARIABLES DEL NAVBAR/MODALES DE TU COMPAÑERA ===
  loginData = { email: '', password: '' };
  registerData = { firstName: '', lastName: '', email: '', password: '' };

  constructor(
    private router: Router,
    private bookingService: BookingService,
    private authService: AuthService // <-- Inyectamos el authService
  ) {}

  ngOnInit() {
    this.resumenActual = this.bookingService.obtenerResumen();
    this.asientosSeleccionados = [...this.resumenActual.asientos];
  }

  // === LÓGICA DE ASIENTOS ===
  seleccionarAsiento(asientoId: string) {
    if (this.asientosOcupados.includes(asientoId)) return; 
    const index = this.asientosSeleccionados.indexOf(asientoId);
    if (index > -1) {
      this.asientosSeleccionados.splice(index, 1);
    } else {
      if (this.asientosSeleccionados.length >= 10) {
        alert('Solo puedes seleccionar un máximo de 10 asientos por compra.');
        return;
      }
      this.asientosSeleccionados.push(asientoId);
    }
  }

  estadoAsiento(asientoId: string): string {
    if (this.asientosOcupados.includes(asientoId)) return 'ocupado';
    if (this.asientosSeleccionados.includes(asientoId)) return 'seleccionado';
    if (this.asientosDiscapacitados.includes(asientoId)) return 'discapacitado';
    return 'disponible';
  }

  irSiguientePaso() {
    if (this.asientosSeleccionados.length === 0) {
      alert('Por favor selecciona al menos un asiento.');
      return;
    }
    const isUserLoggedIn = this.authService.isLoggedIn();
    this.bookingService.guardarAsientos(this.asientosSeleccionados);
    console.log("¿El sistema cree que estoy logueado?:", isUserLoggedIn);

    if (isUserLoggedIn) {
      // Solo si el servicio confirma que el token es válido, pasamos
      this.router.navigate(['/tickets']);
    } else {
      // Si no, forzamos la apertura del panel lateral
      console.log("No hay sesión. Abriendo panel de login...");
      const loginTrigger = document.querySelector('.user-icon') as HTMLElement;
      if (loginTrigger) {
        loginTrigger.click(); // Esto abre el panel derecho 
      } else {
        // Intento por data-target si no encuentra la clase
        const alternativeTrigger = document.querySelector('[data-bs-target="#loginOffcanvas"]') as HTMLElement;
        alternativeTrigger?.click();
      }
    }
  }

}