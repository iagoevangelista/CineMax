import { Component } from '@angular/core';
import { BookingService } from '../../../services/booking';
import { Router } from '@angular/router';
@Component({
  selector: 'app-snacks',
  imports: [],
  templateUrl: './snacks.html',
  styleUrl: './snacks.css',
})
export class Snacks {

  productos = [
    { id: 1, nombre: 'Combo Dúo', precio: 45.00, desc: '2 Gaseosas + 1 Popcorn Gigante', imagen: 'assets/combo1.png', cantidad: 0 },
    { id: 2, nombre: 'Popcorn Mediana', precio: 15.00, desc: 'Salada o Dulce', imagen: 'assets/pop.png', cantidad: 0 },
    // ... más productos
  ];
  constructor(private bookingService: BookingService, private router: Router) {}



  sumar(prod: any) {
    prod.cantidad++;
  }

  restar(prod: any) {
    if (prod.cantidad > 0) prod.cantidad--;
  }

  get totalPagar() {
    return this.productos.reduce((acc, p) => acc + (p.precio * p.cantidad), 0);
  }

  continuarAlPago() {
    const seleccionados = this.productos.filter(p => p.cantidad > 0);
    this.bookingService.guardarSnacks(seleccionados);
    this.router.navigate(['/resumen-pago']); // O tu ruta de tickets
  }





}


