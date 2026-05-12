import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // <-- Necesario para los modales
import { Router, RouterModule } from '@angular/router';
import { BookingService } from '../../../services/booking';
import { AuthService } from '../../../services/auth.service'; // <-- Servicio de tu compañera

@Component({
  selector: 'app-seats',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // <-- Agregamos FormsModule
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
  
    // Guardamos los asientos en el servicio para no perderlos
    this.bookingService.guardarAsientos(this.asientosSeleccionados);
  
    // Verificamos si el usuario está logueado
    // Nota: Suponiendo que tu AuthService tiene un método o variable 'isLoggedIn'
    const isUserLoggedIn = !!localStorage.getItem('token'); // Una forma rápida de verificar
  
    if (isUserLoggedIn) {
      // Si está logueado, vamos directo a tickets
      this.router.navigate(['/tickets']);
    } else {
      // Si NO está logueado, disparamos el modal de login que está en el Layout
      // Buscamos el botón "fantasma" o el ID del offcanvas
      const loginTrigger = document.querySelector('[data-bs-target="#loginOffcanvas"]') as HTMLElement;
      if (loginTrigger) {
        loginTrigger.click();
      } else {
        alert('Por favor, inicia sesión para continuar con tu compra.');
      }
    }
  }

}