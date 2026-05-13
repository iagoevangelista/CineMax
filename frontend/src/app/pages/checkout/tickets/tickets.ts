import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { BookingService } from '../../../services/booking.js';

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tickets.html',
  styleUrl: './tickets.css'
})
export class Tickets implements OnInit {
  resumenActual: any;
  cantidadAsientos = 0;
  
  // Precios base
  precioAdulto = 25.00;
  precioNino = 18.00;

  // Contadores
  entradas = {
    adulto: 0,
    nino: 0
  };

  constructor(
    private router: Router,
    private bookingService: BookingService
  ) {}

  ngOnInit() {
    this.resumenActual = this.bookingService.obtenerResumen();
    this.cantidadAsientos = this.resumenActual.asientos.length;
    
    // Si entran aquí sin elegir asientos, los regresamos
    if (this.cantidadAsientos === 0) {
      this.router.navigate(['/seats']);
    }
  }

  get totalEntradasElegidas() {
    return this.entradas.adulto + this.entradas.nino;
  }

  get totalPagar() {
    return (this.entradas.adulto * this.precioAdulto) + (this.entradas.nino * this.precioNino);
  }

  cambiarCantidad(tipo: 'adulto' | 'nino', incremento: number) {
    const nuevaCantidad = this.entradas[tipo] + incremento;
    
    // Validaciones
    if (nuevaCantidad < 0) return; // No menos de 0
    if (incremento > 0 && this.totalEntradasElegidas >= this.cantidadAsientos) return; // No más que los asientos

    this.entradas[tipo] = nuevaCantidad;
  }

  irSiguientePaso() {
    if (this.totalEntradasElegidas < this.cantidadAsientos) {
      alert(`Te faltan elegir ${this.cantidadAsientos - this.totalEntradasElegidas} entradas para tus asientos.`);
      return;
    }
    
    // Guardamos las entradas y el total en el servicio y vamos a dulcería
    this.resumenActual.boletos = this.entradas;
    this.resumenActual.totalPagar = this.totalPagar;
    
    this.router.navigate(['/snacks']).then(navego => {
      if (navego) {
        console.log("¡Navegación exitosa!");
      } else {
        console.error("La navegación falló. Revisa si la ruta '/confiteria' existe en app.routes.ts");
      }
    }).catch(err => {
      console.error("Error crítico en la navegación:", err);
    });
  }

  regresar() {
    this.router.navigate(['/seats']);
  }
}